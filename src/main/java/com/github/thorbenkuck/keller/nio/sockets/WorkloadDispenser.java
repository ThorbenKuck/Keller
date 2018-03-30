package com.github.thorbenkuck.keller.nio.sockets;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

class WorkloadDispenser {

	private final Set<SelectorInformation> storedInformation = new HashSet<>();
	private final Map<SocketChannel, SelectorInformation> channelSelectorMap = new HashMap<>();
	private final AtomicReference<SelectorInformation> pointer = new AtomicReference<>();
	private final DisconnectedListener disconnectedListener;
	private final ReceivedListener receivedListener;
	private final Deserializer deserializer;
	private final Lock lock = new ReentrantLock(true);
	private Supplier<Integer> bufferSize;
	private Supplier<ExecutorService> executorServiceSupplier;
	private int maxWorkload;

	WorkloadDispenser(DisconnectedListener disconnectedListener, ReceivedListener receivedListener, Deserializer deserializer) {
		this(disconnectedListener, receivedListener, deserializer, 1024);
	}

	WorkloadDispenser(DisconnectedListener disconnectedListener, ReceivedListener receivedListener, Deserializer deserializer, int maxWorkload) {
		this.disconnectedListener = disconnectedListener;
		this.receivedListener = receivedListener;
		this.deserializer = deserializer;
		this.maxWorkload = maxWorkload;
	}

	public void appeal(SocketChannel socketChannel) throws IOException {
		try {
			lock.lock();
			if (!isCurrentUsable()) {
				setNew(pointer.get());
			}

			appealOnCurrent(socketChannel);
			System.out.println("\n\n\nElements stored: " + channelSelectorMap.size() + "(" + storedInformation.size() + ")\n\n\n");
		} finally {
			lock.unlock();
		}
	}

	public void remove(SocketChannel socketChannel) {
		try {
			lock.lock();
			SelectorInformation selectorInformation = channelSelectorMap.remove(socketChannel);
			System.out.println("workload: " + selectorInformation.getWorkload());
			socketChannel.keyFor(selectorInformation.selector()).cancel();
			int newWorkload = selectorInformation.getWorkload() - 1;
			if (newWorkload == 0) {
				stop(selectorInformation);
			} else {
				selectorInformation.setWorkload(newWorkload);
			}
			System.out.println("\n\n\nElements stored: " + channelSelectorMap.size() + "(" + storedInformation.size() + ")\n\n\n");
		} finally {
			lock.unlock();
		}
	}

	public void setMaxWorkload(int to) {
		this.maxWorkload = to;
	}

	private void stop(SelectorInformation selector) {
		System.out.println("Stopping and removing out of date selector");
		selector.getReceiveObjectListener().stop();
		storedInformation.remove(selector);
		// This is so harsh..
		// Why Oracle? Why
		// Did you design NIO
		// in this way? Who
		// approved this
		// Design?!
		selector.selector().wakeup();
	}

	private void start(SelectorInformation selector) {
		executorServiceSupplier.get().submit(selector.getReceiveObjectListener());
	}

	private void appealOnCurrent(SocketChannel socketChannel) throws ClosedChannelException {
		SelectorInformation selectorInformation = pointer.get();
		socketChannel.register(selectorInformation.selector(), SelectionKey.OP_READ);
		channelSelectorMap.put(socketChannel, selectorInformation);
		selectorInformation.incrementWorkload();
	}

	private boolean isCurrentUsable() throws IOException {
		return pointer.get() != null && pointer.get().getWorkload() < maxWorkload;
	}

	private void setNew(SelectorInformation old) throws IOException {
		if (old != null) {
			System.out.println("Storing given SelectorInformation");
			storedInformation.add(old);
		}

		System.out.println("Searching for a new Selector to use");
		SelectorInformation usable = getOtherUsable();
		if (usable != null) {
			System.out.println("Found usable Selector, that already is instantiated. Setting pointer");
			pointer.set(usable);
		} else {
			System.out.println("Create new Selector ..");
			Selector selector = Selector.open();
			final SelectorInformation information = new SelectorInformation(selector, createReceiveObjectListener(selector));
			pointer.set(information);
			storedInformation.add(information);
			start(information);
		}
	}

	private ReceiveObjectListener createReceiveObjectListener(Selector selector) {
		return new ReceiveObjectListener(selector, disconnectedListener, receivedListener, deserializer, bufferSize, executorServiceSupplier);
	}

	public void setBufferSize(Supplier<Integer> bufferSize) {
		this.bufferSize = bufferSize;
	}

	public void setExecutorServiceSupplier(Supplier<ExecutorService> executorServiceSupplier) {
		this.executorServiceSupplier = executorServiceSupplier;
	}

	public SelectorInformation getOtherUsable() {
		for (SelectorInformation selectorInformation : storedInformation) {
			if (selectorInformation.getWorkload() < maxWorkload) {
				return selectorInformation;
			}
		}
		return null;
	}

	private final class SelectorInformation {

		private final Lock workloadLock = new ReentrantLock(true);
		private final Selector selector;
		private final ReceiveObjectListener receiveObjectListener;
		private int workload = 0;

		private SelectorInformation(Selector selector, ReceiveObjectListener receiveObjectListener) {
			this.selector = selector;
			this.receiveObjectListener = receiveObjectListener;
		}

		public Selector selector() {
			return selector;
		}

		public int getWorkload() {
			return workload;
		}

		public void setWorkload(int workload) {
			this.workload = workload;
		}

		public void incrementWorkload() {
			try {
				workloadLock.lock();
				++workload;
			} finally {
				workloadLock.unlock();
			}
		}

		public void decrementWorkload() {
			try {
				workloadLock.lock();
				--workload;

				if (workload <= 0) {
					workload = 0;
				}
			} finally {
				workloadLock.unlock();
			}
		}

		public ReceiveObjectListener getReceiveObjectListener() {
			return receiveObjectListener;
		}
	}
}
