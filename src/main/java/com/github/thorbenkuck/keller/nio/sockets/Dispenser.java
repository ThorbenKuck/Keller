package com.github.thorbenkuck.keller.nio.sockets;

import com.github.thorbenkuck.keller.datatypes.ConcurrentIterator;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

class Dispenser implements WorkloadDispenser {

	private final Set<LocalSelectorChannel> storedLocalSelectorChannels = new HashSet<>();
	private final AtomicReference<LocalSelectorChannel> pointer = new AtomicReference<>();
	private final DisconnectedListener disconnectedListener;
	private final ReceivedListener receivedListener;
	private final Deserializer deserializer;
	private final AtomicBoolean active = new AtomicBoolean(false);
	private final Lock mutex = new ReentrantLock(true);
	private final Consumer<Exception> exceptionConsumer;
	private final Sender sender;
	private Supplier<Integer> bufferSize;
	private Supplier<ExecutorService> executorServiceSupplier;
	private int maxWorkload;

	Dispenser(DisconnectedListener disconnectedListener, ReceivedListener receivedListener, Deserializer deserializer, Consumer<Exception> consumer, Sender sender) {
		this(disconnectedListener, receivedListener, deserializer, consumer, sender, 1024);
	}

	Dispenser(DisconnectedListener disconnectedListener, ReceivedListener receivedListener, Deserializer deserializer, Consumer<Exception> consumer, Sender sender, int maxWorkload) {
		this.disconnectedListener = disconnectedListener;
		this.receivedListener = receivedListener;
		this.deserializer = deserializer;
		exceptionConsumer = consumer;
		this.sender = sender;
		this.maxWorkload = maxWorkload;
		active.set(true);
	}

	private void start(LocalSelectorChannel selector) {
		executorServiceSupplier.get().submit(selector.getReceiveObjectListener());
	}

	private void appealOnCurrent(SocketChannel socketChannel) throws ClosedChannelException {
		System.out.println("Selecting current pointer");
		LocalSelectorChannel localSelectorChannel = pointer.get();
		System.out.println("Adding SocketChannel to LocalSelectorChannel");
		localSelectorChannel.add(socketChannel);
	}

	private boolean isCurrentUsable() {
		return pointer.get() != null && pointer.get().getWorkload() < maxWorkload && pointer.get().selector.isOpen();
	}

	private void setNew(LocalSelectorChannel old) throws IOException {
		if (old != null) {
			if(!old.selector().isOpen()) {
				old.close();
				synchronized (storedLocalSelectorChannels) {
					storedLocalSelectorChannels.remove(old);
				}
			} else {
				synchronized (storedLocalSelectorChannels) {
					storedLocalSelectorChannels.add(old);
				}
			}
		}

		LocalSelectorChannel usable = getOtherUsable();
		if (usable != null) {
			pointer.set(usable);
		} else {
			Selector selector = Selector.open();
			final LocalSelectorChannel information = new LocalSelectorChannel(selector, createReceiveObjectListener(selector));
			pointer.set(information);
			synchronized (storedLocalSelectorChannels) {
				storedLocalSelectorChannels.add(information);
			}
			start(information);
		}
	}

	private ReceiveObjectListener createReceiveObjectListener(Selector selector) {
		return new ReceiveObjectListener(selector, disconnectedListener, receivedListener, deserializer, bufferSize, executorServiceSupplier, exceptionConsumer);
	}

	private void clearFrom(SocketChannel socketChannel, SelectorChannel localSelectorChannel) throws IOException {
		localSelectorChannel.remove(socketChannel);
		int newWorkload = localSelectorChannel.getWorkload();
		if (newWorkload == 0) {
			localSelectorChannel.close();
		}
//		printCurrentStand();
	}

	private LocalSelectorChannel getSelectorChannel(SocketChannel socketChannel) {
		final List<LocalSelectorChannel> localSelectorChannels;
		synchronized (storedLocalSelectorChannels) {
			localSelectorChannels = new ArrayList<>(storedLocalSelectorChannels);
		}
		for(LocalSelectorChannel channel : localSelectorChannels) {
			if(channel.contains(socketChannel)) {
				return channel;
			}
		}
		return null;
	}

	private void assignLowest() {
		final List<LocalSelectorChannel> selectorChannels;
		synchronized (storedLocalSelectorChannels) {
			selectorChannels = new ArrayList<>(storedLocalSelectorChannels);
		}

		LocalSelectorChannel lowest = null;

		for(LocalSelectorChannel selectorChannel : selectorChannels) {
			if(lowest == null) {
				lowest = selectorChannel;
			} else {
				if(selectorChannel.getWorkload() < lowest.getWorkload()) {
					lowest = selectorChannel;
				}
			}
		}
	}

	private void checkAllSelectorChannels() {
		final List<LocalSelectorChannel> selectorChannels;
		synchronized (storedLocalSelectorChannels) {
			selectorChannels = new ArrayList<>(storedLocalSelectorChannels);
		}

		for(LocalSelectorChannel selectorChannel : selectorChannels) {
			pointer.set(selectorChannel);
			if(!isCurrentUsable()) {
				synchronized (storedLocalSelectorChannels) {
					storedLocalSelectorChannels.remove(selectorChannel);
				}
				try {
					selectorChannel.close();
				} catch (IOException e) {
					exceptionConsumer.accept(e);
				}
			}
		}
	}

	@Override
	public void appeal(SocketChannel socketChannel) throws IOException {
		try {
			System.out.println("[APPEAL] acquiring mutex");
			mutex.lock();
			System.out.println("[APPEAL] acquired mutex");
			if(!active.get()) {
				System.out.println("Not active... returning..");
				return;
			}

			if(getSelectorChannel(socketChannel) != null) {
				System.out.println("[ERROR] SocketChannel already appealed!");
				return;
			}

			if (!isCurrentUsable()) {
				System.out.println("Current pointer is not usable! Updating..");
				LocalSelectorChannel current = pointer.get();
				setNew(current);
			}

			System.out.println("Appealing on current");
			appealOnCurrent(socketChannel);
//			printCurrentStand();
		} catch (Throwable throwable) {
			// This check is needed, because
			// something appears to be wrong
			// at the current time.. After
			// a certain time, the server simply
			// stops, without warning, without
			// Exception, nothing. Maybe, we will
			// catch a rouge Exception this way
			throwable.printStackTrace(System.out);
		} finally {
			mutex.unlock();
			System.out.println("[APPEAL] released mutex");
		}
	}

	@Override
	public List<SocketChannel> collectCorpses() {
//		System.out.println("\n\nTrying to collect corpses ..\n\n");
		final List<SocketChannel> socketChannels = new ArrayList<>();
		if(!active.get()) {
			System.out.println("Not active. Returning.");
			return new ArrayList<>();
		}
		final List<LocalSelectorChannel> keySet;
		synchronized (storedLocalSelectorChannels) {
			if(storedLocalSelectorChannels.isEmpty()) {
				System.out.println("No running selector channels");
				return socketChannels;
			}
			System.out.println("Copying stored selector channels");
			keySet = new ArrayList<>(storedLocalSelectorChannels);
		}
		try {
			System.out.println("[COLLECT_CORPSES] acquiring mutex");
			mutex.lock();
			System.out.println("[COLLECT_CORPSES] acquired mutex");
			System.out.println("Searching in " + keySet);
			for (LocalSelectorChannel localSelectorChannel : keySet) {
				for(SocketChannel socketChannel : localSelectorChannel) {
					System.out.println("Looking at: " + socketChannel);
					if (!socketChannel.isConnected() || !socketChannel.isOpen()) {
						System.out.println("Found corpse");
						socketChannels.add(socketChannel);
						clearFrom(socketChannel, localSelectorChannel);
					}
				}
			}
		} catch (Throwable throwable) {
			// This check is needed, because
			// something appears to be wrong
			// at the current time.. After
			// a certain time, the server simply
			// stops, without warning, without
			// Exception, nothing. Maybe, we will
			// catch a rouge Exception this way
			throwable.printStackTrace(System.out);
		}  finally {
			mutex.unlock();
			System.out.println("[COLLECT_CORPSES] released mutex");
		}
		return socketChannels;
	}

	@Override
	public List<SocketChannel> deepCollectCorpses() {
		collectCorpses();
		final List<SocketChannel> socketChannels = new ArrayList<>();
		try {
			System.out.println("[DEEP_COLLECT_CORPSES] acquiring mutex");
			mutex.lock();
			System.out.println("[DEEP_COLLECT_CORPSES] acquired mutex");
			final List<SelectorChannel> selectorChannels;
			synchronized (storedLocalSelectorChannels) {
				if(storedLocalSelectorChannels.isEmpty()) {
					return socketChannels;
				}
				selectorChannels = new ArrayList<>(storedLocalSelectorChannels);
			}

			for(SelectorChannel selectorChannel : selectorChannels) {
				final List<SocketChannel> setChannels = new ArrayList<>(selectorChannel.getSocketChannels());
				for(SocketChannel socketChannel : setChannels) {
					if(selectorChannel.getWorkload() == 0) {
						continue;
					}
					String toSend = "PING";
					try {
						if(!sender.send(toSend, socketChannel)) {
							selectorChannel.remove(socketChannel);
							socketChannels.add(socketChannel);
						}
					} catch (IOException e) {
						// We found an corpse!
						selectorChannel.remove(socketChannel);
						socketChannels.add(socketChannel);
					}
				}
			}
			checkAllSelectorChannels();
			assignLowest();
		} catch (Throwable throwable) {
			// This check is needed, because
			// something appears to be wrong
			// at the current time.. After
			// a certain time, the server simply
			// stops, without warning, without
			// Exception, nothing. Maybe, we will
			// catch a rough Exception this way
			throwable.printStackTrace(System.out);
		} finally {
			mutex.unlock();
			System.out.println("[DEEP_COLLECT_CORPSES] released mutex");
		}

		return socketChannels;
	}

	@Override
	public void clearAll() {
		try {
			System.out.println("[CLEAR_ALL] acquiring mutex");
			mutex.lock();
			System.out.println("[CLEAR_ALL] acquired mutex");
			final List<LocalSelectorChannel> selectorChannels;
			synchronized (storedLocalSelectorChannels) {
				if(storedLocalSelectorChannels.isEmpty()) {
					return;
				}
				selectorChannels = new ArrayList<>(storedLocalSelectorChannels);
			}

			for(LocalSelectorChannel selectorChannel : selectorChannels) {
				selectorChannel.close();
				synchronized (storedLocalSelectorChannels) {
					storedLocalSelectorChannels.remove(selectorChannel);
				}
			}
			setNew(null);
		} catch (Throwable throwable) {
			// This check is needed, because
			// something appears to be wrong
			// at the current time.. After
			// a certain time, the server simply
			// stops, without warning, without
			// Exception, nothing. Maybe, we will
			// catch a rough Exception this way
			throwable.printStackTrace(System.out);
		} finally {
			mutex.unlock();
			System.out.println("[CLEAR_ALL] released mutex");
		}
	}

	@Override
	public void remove(SocketChannel socketChannel) {
		try {
			System.out.println("[REMOVE] acquiring mutex");
			mutex.lock();
			System.out.println("[REMOVE] acquired mutex");
			if(!active.get()) {
				return;
			}
			LocalSelectorChannel localSelectorChannel = getSelectorChannel(socketChannel);
			if(localSelectorChannel == null) {
				return;
			}
			clearFrom(socketChannel, localSelectorChannel);
		} catch (Throwable throwable) {
			// This check is needed, because
			// something appears to be wrong
			// at the current time.. After
			// a certain time, the server simply
			// stops, without warning, without
			// Exception, nothing. Maybe, we will
			// catch a rough Exception this way
			throwable.printStackTrace(System.out);
		} finally {
			mutex.unlock();
			System.out.println("[REMOVE] released mutex");
		}
	}

	@Override
	public void setMaxWorkload(int to) {
		this.maxWorkload = to;
	}

	@Override
	public int countSelectorChannels() {
		synchronized (storedLocalSelectorChannels) {
			return storedLocalSelectorChannels.size();
		}
	}

	@Override
	public int countConnectNodes() {
		int size = 0;
		synchronized (storedLocalSelectorChannels) {
			for(LocalSelectorChannel localSelectorChannel : storedLocalSelectorChannels) {
				size += localSelectorChannel.getWorkload();
			}
		}
		return size;
	}

	public void setBufferSize(Supplier<Integer> bufferSize) {
		this.bufferSize = bufferSize;
	}

	public void setExecutorServiceSupplier(Supplier<ExecutorService> executorServiceSupplier) {
		this.executorServiceSupplier = executorServiceSupplier;
	}

	public LocalSelectorChannel getOtherUsable() {
		for (LocalSelectorChannel localSelectorChannel : storedLocalSelectorChannels) {
			if (localSelectorChannel.getWorkload() < maxWorkload) {
				return localSelectorChannel;
			}
		}
		return null;
	}

	public void shutdown() {
		try {
			System.out.println("[SHUTDOWN] acquiring mutex");
			mutex.lock();
			System.out.println("[SHUTDOWN] acquired mutex");
			active.set(false);
			final List<LocalSelectorChannel> copy;
			synchronized (storedLocalSelectorChannels) {
				copy = new ArrayList<>(storedLocalSelectorChannels);
			}
			for(LocalSelectorChannel localSelectorChannel : copy) {
				try {
					localSelectorChannel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} finally {
			mutex.unlock();
			System.out.println("[SHUTDOWN] released mutex");
		}
	}

	/**
	 * Returns an iterator over elements of type {@code T}.
	 *
	 * @return an Iterator.
	 */
	@Override
	public Iterator<SelectorChannel> iterator() {
		Iterator<SelectorChannel> iterator;
		synchronized (storedLocalSelectorChannels) {
			iterator = new ConcurrentIterator<>(new ArrayList<>(storedLocalSelectorChannels));
		}

		return iterator;
	}

	private final class LocalSelectorChannel implements SelectorChannel {

		private final Selector selector;
		private final ReceiveObjectListener receiveObjectListener;
		private final List<SocketChannel> socketChannels = new ArrayList<>();

		private LocalSelectorChannel(Selector selector, ReceiveObjectListener receiveObjectListener) {
			this.selector = selector;
			this.receiveObjectListener = receiveObjectListener;
		}

		public ReceiveObjectListener getReceiveObjectListener() {
			return receiveObjectListener;
		}

		@Override
		public Selector selector() {
			return selector;
		}

		@Override
		public int getWorkload() {
			synchronized (socketChannels) {
				return socketChannels.size();
			}
		}

		@Override
		public void add(SocketChannel socketChannel) throws ClosedChannelException {
			if(contains(socketChannel)) {
				return;
			}
			synchronized (socketChannels) {
				socketChannels.add(socketChannel);
			}
			socketChannel.register(selector, SelectionKey.OP_READ);
		}

		@Override
		public boolean contains(SocketChannel socketChannel) {
			if(socketChannel == null) {
				return false;
			}
			synchronized (socketChannels) {
				return socketChannels.contains(socketChannel);
			}
		}

		@Override
		public void remove(SocketChannel socketChannel) {
			if(!contains(socketChannel)) {
				return;
			}
			synchronized (socketChannels) {
				socketChannels.remove(socketChannel);
			}
			SelectionKey key = socketChannel.keyFor(selector);
			if(key != null) {
				key.cancel();
			}
		}

		@Override
		public void wakeup() {
			selector.wakeup();
		}

		@Override
		public void close() throws IOException {
			final List<SocketChannel> copy;
			synchronized (socketChannels) {
				copy = new ArrayList<>(socketChannels);
			}
			for(SocketChannel socketChannel : copy) {
				remove(socketChannel);
			}
			receiveObjectListener.stop();
			wakeup();
			selector.close();
			for(SocketChannel socketChannel : copy) {
				socketChannel.close();
			}
		}

		@Override
		public List<SocketChannel> getSocketChannels() {
			synchronized (socketChannels) {
				return new ArrayList<>(socketChannels);
			}
		}

		@Override
		public synchronized Iterator<SocketChannel> iterator() {
			synchronized (socketChannels) {
				return new ConcurrentIterator<>(socketChannels);
			}
		}

		@Override
		public String toString() {
			synchronized (socketChannels) {
				return "LocalSelectorChannel{" + selector + ", " + socketChannels.toString() + "}";
			}
		}
	}
}
