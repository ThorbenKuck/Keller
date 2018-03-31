package com.github.thorbenkuck.keller.nio.sockets;

import com.github.thorbenkuck.keller.datatypes.ConcurrentIterator;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

class Dispenser implements WorkloadDispenser {

	private final Set<LocalSelectorChannel> storedLocalSelectorChannels = new HashSet<>();
	private final AtomicReference<LocalSelectorChannel> pointer = new AtomicReference<>();
	private final DisconnectedListener disconnectedListener;
	private final ReceivedListener receivedListener;
	private final Deserializer deserializer;
	private final AtomicBoolean active = new AtomicBoolean(false);
	private final Semaphore mutex = new Semaphore(1);
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
		this.exceptionConsumer = consumer;
		this.sender = sender;
		this.maxWorkload = maxWorkload;
		active.set(true);
	}

	private List<LocalSelectorChannel> copySelectorChannels() {
		final List<LocalSelectorChannel> localSelectorChannels;
		synchronized (storedLocalSelectorChannels) {
			localSelectorChannels = new ArrayList<>(storedLocalSelectorChannels);
		}
		return localSelectorChannels;
	}

	private void add(LocalSelectorChannel localSelectorChannel) {
		synchronized (storedLocalSelectorChannels) {
			storedLocalSelectorChannels.add(localSelectorChannel);
		}
	}

	private void remove(LocalSelectorChannel localSelectorChannel) {
		synchronized (storedLocalSelectorChannels) {
			storedLocalSelectorChannels.remove(localSelectorChannel);
		}
	}

	private LocalSelectorChannel getPointer() {
		LocalSelectorChannel channel;
		synchronized (pointer) {
			channel = pointer.get();
		}
		return channel;
	}

	private void setPointer(LocalSelectorChannel channel) {
		synchronized (pointer) {
			pointer.set(channel);
		}
	}

	private void acquire() throws InterruptedException {
		mutex.acquire();
	}

	private void release() {
		mutex.release();
	}

	private void handle(Exception e) {
		exceptionConsumer.accept(e);
	}

	private void start(LocalSelectorChannel selector) {
		executorServiceSupplier.get().submit(selector.getReceiveObjectListener());
	}

	private void appealOnCurrent(SocketChannel socketChannel) throws ClosedChannelException {
		System.out.println("Selecting current pointer");
		LocalSelectorChannel currentPointer = getPointer();
		System.out.println("Adding SocketChannel to LocalSelectorChannel");
		currentPointer.add(socketChannel);
	}

	private boolean isCurrentUsable() {
		SelectorChannel channel = getPointer();
		return channel != null && channel.getWorkload() < maxWorkload && channel.isOpen();
	}

	private LocalSelectorChannel getOtherUsable() {
		final List<LocalSelectorChannel> selectorChannels = copySelectorChannels();
		for (LocalSelectorChannel localSelectorChannel : selectorChannels) {
			if (localSelectorChannel.getWorkload() < maxWorkload) {
				return localSelectorChannel;
			}
		}
		return null;
	}

	private void setNew(LocalSelectorChannel old) throws IOException {
		if (old != null) {
			if (!old.isOpen()) {
				remove(old);
				try {
					old.close();
				} catch (IOException e) {
					handle(e);
				}
			} else {
				add(old);
			}
		}

		LocalSelectorChannel usable = getOtherUsable();
		if (usable != null) {
			setPointer(usable);
		} else {
			Selector selector = Selector.open();
			final LocalSelectorChannel channel = new LocalSelectorChannel(selector, createReceiveObjectListener(selector));
			start(channel);
			add(channel);
			setPointer(channel);
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
	}

	private LocalSelectorChannel getSelectorChannel(SocketChannel socketChannel) {
		if(isEmpty()) {
			return null;
		}
		final List<LocalSelectorChannel> localSelectorChannels = copySelectorChannels();
		for (LocalSelectorChannel channel : localSelectorChannels) {
			if (channel.contains(socketChannel)) {
				return channel;
			}
		}
		return null;
	}

	private boolean operationsNeeded() {
		return !isEmpty() && active.get();
	}

	void setBufferSize(Supplier<Integer> bufferSize) {
		this.bufferSize = bufferSize;
	}

	void setExecutorServiceSupplier(Supplier<ExecutorService> executorServiceSupplier) {
		this.executorServiceSupplier = executorServiceSupplier;
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
		final List<LocalSelectorChannel> selectorChannels = copySelectorChannels();
		synchronized (storedLocalSelectorChannels) {
			for (LocalSelectorChannel localSelectorChannel : selectorChannels) {
				size += localSelectorChannel.getWorkload();
			}
		}
		return size;
	}

	@Override
	public boolean isEmpty() {
		boolean empty;
		synchronized (storedLocalSelectorChannels) {
			empty = (storedLocalSelectorChannels.isEmpty());
		}
		return empty;
	}

	@Override
	public List<SocketChannel> collectCorpses() {
		final List<SocketChannel> socketChannels = new ArrayList<>();

		try {
			System.out.println("[COLLECT_CORPSES] acquiring mutex");
			acquire();
			System.out.println("[COLLECT_CORPSES] acquired mutex");
			if(!operationsNeeded()) {
				return socketChannels;
			}
			final List<LocalSelectorChannel> keySet = copySelectorChannels();
			for (LocalSelectorChannel localSelectorChannel : keySet) {
				for (SocketChannel socketChannel : localSelectorChannel) {
					if (!socketChannel.isConnected() || !socketChannel.isOpen()) {
						socketChannels.add(socketChannel);
						clearFrom(socketChannel, localSelectorChannel);
					}
				}
			}
		} catch (InterruptedException | IOException e) {
			handle(e);
		} finally {
			release();
			clearEmpty();
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
			acquire();
			System.out.println("[DEEP_COLLECT_CORPSES] acquired mutex");
			if(!operationsNeeded()) {
				return socketChannels;
			}
			final List<LocalSelectorChannel> selectorChannels = copySelectorChannels();

			for (SelectorChannel selectorChannel : selectorChannels) {
				final List<SocketChannel> setChannels = new ArrayList<>(selectorChannel.getSocketChannels());
				for (SocketChannel socketChannel : setChannels) {
					if (selectorChannel.getWorkload() == 0) {
						continue;
					}
					String toSend = "PING";
					try {
						if (!sender.send(toSend, socketChannel)) {
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
			cleanUpSelectorChannels();
		} catch (InterruptedException e) {
			handle(e);
		} finally {
			release();
			System.out.println("[DEEP_COLLECT_CORPSES] released mutex");
		}

		return socketChannels;
	}

	@Override
	public List<SelectorChannel> clearEmpty() {
		final List<SelectorChannel> returnValue = new ArrayList<>();

		try {
			System.out.println("[COLLECT_CORPSES] acquiring mutex");
			acquire();
			System.out.println("[COLLECT_CORPSES] acquired mutex");
			if(!operationsNeeded()) {
				return returnValue;
			}
			final List<LocalSelectorChannel> keySet = copySelectorChannels();
			for (LocalSelectorChannel localSelectorChannel : keySet) {
				System.out.println("looking at: " + localSelectorChannel);
				if(localSelectorChannel.isEmpty() || localSelectorChannel.getWorkload() == 0) {
					remove(localSelectorChannel);
					localSelectorChannel.close();
				}
			}
		} catch (InterruptedException | IOException e) {
			handle(e);
		} finally {
			release();
		}

		return returnValue;
	}

	@Override
	public void clearAll() {
		try {
			System.out.println("[CLEAR_ALL] acquiring mutex");
			acquire();
			System.out.println("[CLEAR_ALL] acquired mutex");
			if(!operationsNeeded()) {
				return;
			}
			final List<LocalSelectorChannel> selectorChannels = copySelectorChannels();

			for (LocalSelectorChannel selectorChannel : selectorChannels) {
				selectorChannel.close();
				remove(selectorChannel);
			}
			setNew(null);
		} catch (InterruptedException | IOException e) {
			handle(e);
		} finally {
			release();
			System.out.println("[CLEAR_ALL] released mutex");
		}
	}

	@Override
	public void cleanUpSelectorChannels() {
		try {
			System.out.println("[CLEAN_UP] released mutex");
			acquire();
			System.out.println("[CLEAN_UP] released mutex");
			if(!operationsNeeded()) {
				return;
			}
			final List<LocalSelectorChannel> selectorChannels = copySelectorChannels();
			for (LocalSelectorChannel selectorChannel : selectorChannels) {
				setPointer(selectorChannel);
				if (!isCurrentUsable()) {
					remove(selectorChannel);
					try {
						selectorChannel.close();
					} catch (IOException e) {
						handle(e);
					}
				}
			}
		} catch (InterruptedException e) {
			handle(e);
		} finally {
			release();
			System.out.println("[CLEAN_UP] released mutex");
		}
	}

	@Override
	public void assignLowestSelectorChannel() {
		try {
			acquire();
			if(!operationsNeeded()) {
				return;
			}
			final List<LocalSelectorChannel> selectorChannels = copySelectorChannels();
			LocalSelectorChannel lowest = null;

			for (LocalSelectorChannel selectorChannel : selectorChannels) {
				if (lowest == null) {
					lowest = selectorChannel;
				} else {
					if (selectorChannel.getWorkload() < lowest.getWorkload()) {
						lowest = selectorChannel;
					}
				}
			}

			if(lowest != null) {
				setPointer(lowest);
			}
		} catch (InterruptedException e) {
			handle(e);
		} finally {
			release();
		}
	}

	@Override
	public void shutdown() {
		try {
			System.out.println("[SHUTDOWN] acquiring mutex");
			acquire();
			System.out.println("[SHUTDOWN] acquired mutex");
			if(!active.get()) {
				return;
			}
			active.set(false);
			final List<LocalSelectorChannel> copy = copySelectorChannels();
			for (LocalSelectorChannel localSelectorChannel : copy) {
				try {
					localSelectorChannel.close();
				} catch (IOException e) {
					handle(e);
				}
			}
		} catch (InterruptedException e) {
			handle(e);
		} finally {
			release();
			System.out.println("[SHUTDOWN] released mutex");
		}
	}

	@Override
	public void appeal(SocketChannel socketChannel) throws IOException {
		try {
			System.out.println("[APPEAL] acquiring mutex");
			acquire();
			System.out.println("[APPEAL] acquired mutex");
			if(!active.get()) {
				return;
			}

			// This should (in theory)
			// never happen. However,
			// if an develop decides to
			// appeal the same socketChannel
			// twice, this is mandatory
			// to ensure no bloating
			if (getSelectorChannel(socketChannel) != null) {
				System.out.println("[ERROR] SocketChannel already appealed!");
				return;
			}

			if (!isCurrentUsable()) {
				System.out.println("Current pointer is not usable! Updating..");
				setNew(getPointer());
			}

			System.out.println("Appealing on current");
			appealOnCurrent(socketChannel);
		} catch (InterruptedException e) {
			handle(e);
		} finally {
			release();
			System.out.println("[APPEAL] released mutex");
		}
	}

	@Override
	public void remove(SocketChannel socketChannel) {
		try {
			System.out.println("[REMOVE] acquiring mutex");
			acquire();
			System.out.println("[REMOVE] acquired mutex");
			if(!operationsNeeded()) {
				return;
			}
			LocalSelectorChannel localSelectorChannel = getSelectorChannel(socketChannel);
			if (localSelectorChannel == null) {
				return;
			}
			clearFrom(socketChannel, localSelectorChannel);
		} catch (InterruptedException | IOException e) {
			handle(e);
		} finally {
			release();
		}
	}

	@Override
	public void setMaxWorkload(int to) {
		this.maxWorkload = to;
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
			if(!selector.isOpen()) {
			return;
			}
			if (contains(socketChannel)) {
				return;
			}
			synchronized (socketChannels) {
				socketChannels.add(socketChannel);
			}

			socketChannel.register(selector, SelectionKey.OP_READ);
		}

		@Override
		public boolean contains(SocketChannel socketChannel) {
			if (socketChannel == null) {
				return false;
			}
			synchronized (socketChannels) {
				return socketChannels.contains(socketChannel);
			}
		}

		@Override
		public boolean isOpen() {
			return selector.isOpen();
		}

		@Override
		public void remove(SocketChannel socketChannel) {
			if (!contains(socketChannel)) {
				return;
			}
			synchronized (socketChannels) {
				socketChannels.remove(socketChannel);
			}
			SelectionKey key = socketChannel.keyFor(selector);
			if (key != null) {
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
			for (SocketChannel socketChannel : copy) {
				remove(socketChannel);
			}
			receiveObjectListener.stop();
			wakeup();
			selector.close();
			for (SocketChannel socketChannel : copy) {
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
				return "LocalSelectorChannel{#socketChannels=" + socketChannels.size() + ", workload=" + getWorkload() + ", empty=" + isEmpty() + ", selector=" + selector + ", socketChannels=" + socketChannels.toString() + "}";
			}
		}

		public boolean isEmpty() {
			synchronized (socketChannels) {
				return socketChannels.isEmpty();
			}
		}
	}
}
