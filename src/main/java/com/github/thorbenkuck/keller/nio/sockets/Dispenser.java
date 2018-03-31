package com.github.thorbenkuck.keller.nio.sockets;

import java.io.IOException;
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

	private final Set<SelectorChannel> storedSelectorChannels = new HashSet<>();
	private final AtomicReference<SelectorChannel> pointer = new AtomicReference<>();
	private final DisconnectedListener disconnectedListener;
	private final ReceivedListener receivedListener;
	private final Deserializer deserializer;
	private final AtomicBoolean active = new AtomicBoolean(false);
	private final Lock mutex = new ReentrantLock(true);
	private final Consumer<Exception> exceptionConsumer;
	private Supplier<Integer> bufferSize;
	private Supplier<ExecutorService> executorServiceSupplier;
	private int maxWorkload;

	Dispenser(DisconnectedListener disconnectedListener, ReceivedListener receivedListener, Deserializer deserializer, Consumer<Exception> consumer) {
		this(disconnectedListener, receivedListener, deserializer, consumer, 1024);
	}

	Dispenser(DisconnectedListener disconnectedListener, ReceivedListener receivedListener, Deserializer deserializer, Consumer<Exception> consumer, int maxWorkload) {
		this.disconnectedListener = disconnectedListener;
		this.receivedListener = receivedListener;
		this.deserializer = deserializer;
		exceptionConsumer = consumer;
		this.maxWorkload = maxWorkload;
		active.set(true);
	}

	private void stop(SelectorChannel selector) {
		selector.getReceiveObjectListener().stop();
		synchronized (storedSelectorChannels) {
			storedSelectorChannels.remove(selector);
		}
		// This is so harsh..
		// Why Oracle? Why
		// Did you design NIO
		// in this way? Who
		// approved this
		// Design?!
		selector.selector().wakeup();
	}

	private void start(SelectorChannel selector) {
		executorServiceSupplier.get().submit(selector.getReceiveObjectListener());
	}

	private void appealOnCurrent(SocketChannel socketChannel) throws ClosedChannelException {
		System.out.println("Selecting current pointer");
		SelectorChannel selectorChannel = pointer.get();
		System.out.println("Registering provided SocketChannel");
		socketChannel.register(selectorChannel.selector(), SelectionKey.OP_READ);
		System.out.println("Adding SocketChannel to SelectorChannel");
		selectorChannel.add(socketChannel);
	}

	private boolean isCurrentUsable() throws IOException {
		return pointer.get() != null && pointer.get().getWorkload() < maxWorkload;
	}

	private void setNew(SelectorChannel old) throws IOException {
		if (old != null) {
			synchronized (storedSelectorChannels) {
				storedSelectorChannels.add(old);
			}
		}

		SelectorChannel usable = getOtherUsable();
		if (usable != null) {
			pointer.set(usable);
		} else {
			Selector selector = Selector.open();
			final SelectorChannel information = new SelectorChannel(selector, createReceiveObjectListener(selector));
			pointer.set(information);
			synchronized (storedSelectorChannels) {
				storedSelectorChannels.add(information);
			}
			start(information);
		}
	}

	private ReceiveObjectListener createReceiveObjectListener(Selector selector) {
		return new ReceiveObjectListener(selector, disconnectedListener, receivedListener, deserializer, bufferSize, executorServiceSupplier, exceptionConsumer);
	}

	private void printCurrentStand() {
		System.out.println("\n\n\nElements stored: " + countConnectNodes() + "(" + countSelectorChannels() + ")\n\n\n");
	}

	private void clearFrom(SocketChannel socketChannel, SelectorChannel selectorChannel) {
		selectorChannel.remove(socketChannel);
		int newWorkload = selectorChannel.getWorkload();
		if (newWorkload == 0) {
			stop(selectorChannel);
		}
//		printCurrentStand();
	}

	private void appealAll(Collection<SocketChannel> channels) throws IOException {
		IOException exception = null;
		for(SocketChannel socketChannel : channels) {
			try {
				if (!isCurrentUsable()) {
					setNew(pointer.get());
				}

				appealOnCurrent(socketChannel);
			} catch (IOException e) {
				if(exception == null) {
					exception = e;
				} else {
					exception.addSuppressed(e);
				}
			}
		}
		if(exception != null) {
			throw exception;
		}
	}

	private SelectorChannel getSelectorChannel(SocketChannel socketChannel) {
		final List<SelectorChannel> selectorChannels;
		synchronized (storedSelectorChannels) {
			selectorChannels = new ArrayList<>(storedSelectorChannels);
		}
		for(SelectorChannel channel : selectorChannels) {
			if(channel.contains(socketChannel)) {
				return channel;
			}
		}
		return null;
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
				SelectorChannel current = pointer.get();
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
		final List<SelectorChannel> keySet;
		synchronized (storedSelectorChannels) {
			if(storedSelectorChannels.isEmpty()) {
				System.out.println("No running selector channels");
				return socketChannels;
			}
			System.out.println("Copying stored selector channels");
			keySet = new ArrayList<>(storedSelectorChannels);
		}
		try {
			System.out.println("[COLLECT_CORPSES] acquiring mutex");
			mutex.lock();
			System.out.println("[COLLECT_CORPSES] acquired mutex");
			System.out.println("Searching in " + keySet);
			for (SelectorChannel selectorChannel : keySet) {
				for(SocketChannel socketChannel : selectorChannel) {
					System.out.println("Looking at: " + socketChannel);
					if (!socketChannel.isConnected() || !socketChannel.isOpen()) {
						System.out.println("Found corpse");
						socketChannels.add(socketChannel);
						clearFrom(socketChannel, selectorChannel);
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
	public void remove(SocketChannel socketChannel) {
		try {
			System.out.println("[REMOVE] acquiring mutex");
			mutex.lock();
			System.out.println("[REMOVE] acquired mutex");
			if(!active.get()) {
				return;
			}
			SelectorChannel selectorChannel = getSelectorChannel(socketChannel);
			if(selectorChannel == null) {
				return;
			}
			clearFrom(socketChannel, selectorChannel);
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
		synchronized (storedSelectorChannels) {
			return storedSelectorChannels.size();
		}
	}

	@Override
	public int countConnectNodes() {
		int size = 0;
		synchronized (storedSelectorChannels) {
			for(SelectorChannel selectorChannel : storedSelectorChannels) {
				size += selectorChannel.getWorkload();
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

	public SelectorChannel getOtherUsable() {
		for (SelectorChannel selectorChannel : storedSelectorChannels) {
			if (selectorChannel.getWorkload() < maxWorkload) {
				return selectorChannel;
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
			final List<SelectorChannel> copy;
			synchronized (storedSelectorChannels) {
				copy = new ArrayList<>(storedSelectorChannels);
			}
			for(SelectorChannel selectorChannel : copy) {
				try {
					selectorChannel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} finally {
			mutex.unlock();
			System.out.println("[SHUTDOWN] released mutex");
		}
	}

	private final class SelectorChannel implements Iterable<SocketChannel> {

		private final Selector selector;
		private final ReceiveObjectListener receiveObjectListener;
		private final List<SocketChannel> socketChannels = new ArrayList<>();

		private SelectorChannel(Selector selector, ReceiveObjectListener receiveObjectListener) {
			this.selector = selector;
			this.receiveObjectListener = receiveObjectListener;
		}

		public Selector selector() {
			return selector;
		}

		public ReceiveObjectListener getReceiveObjectListener() {
			return receiveObjectListener;
		}

		public int getWorkload() {
			synchronized (socketChannels) {
				return socketChannels.size();
			}
		}

		public void add(SocketChannel socketChannel) throws ClosedChannelException {
			if(contains(socketChannel)) {
				return;
			}
			synchronized (socketChannels) {
				socketChannels.add(socketChannel);
			}
			socketChannel.register(selector, SelectionKey.OP_READ);
		}

		public boolean contains(SocketChannel socketChannel) {
			if(socketChannel == null) {
				return false;
			}
			synchronized (socketChannels) {
				return socketChannels.contains(socketChannel);
			}
		}

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

		public void wakeup() {
			selector.wakeup();
		}

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

		public List<SocketChannel> getSocketChannels() {
			synchronized (socketChannels) {
				return new ArrayList<>(socketChannels);
			}
		}

		/**
		 * Returns an iterator over elements of type {@code T}.
		 *
		 * @return an Iterator.
		 */
		@Override
		public synchronized Iterator<SocketChannel> iterator() {
			synchronized (socketChannels) {
				return new SocketChannelIterator(socketChannels);
			}
		}

		@Override
		public String toString() {
			synchronized (socketChannels) {
				return "SelectorChannel{" + selector + ", " + socketChannels.toString() + "}";
			}
		}

		private final class SocketChannelIterator implements Iterator<SocketChannel> {

			private final Queue<SocketChannel> channels;

			private SocketChannelIterator(Collection<SocketChannel> channels) {
				this.channels = new LinkedList<>(channels);
			}

			/**
			 * Returns {@code true} if the iteration has more elements.
			 * (In other words, returns {@code true} if {@link #next} would
			 * return an element rather than throwing an exception.)
			 *
			 * @return {@code true} if the iteration has more elements
			 */
			@Override
			public boolean hasNext() {
				return channels.peek() != null;
			}

			/**
			 * Returns the next element in the iteration.
			 *
			 * @return the next element in the iteration
			 * @throws NoSuchElementException if the iteration has no more elements
			 */
			@Override
			public SocketChannel next() {
				return channels.poll();
			}
		}
	}
}
