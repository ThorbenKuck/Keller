package com.github.thorbenkuck.keller.nio.sockets;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
			printCurrentStand();
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
			lock.unlock();
		}
	}

	public void remove(SocketChannel socketChannel) {
		try {
			lock.lock();
			SelectorInformation selectorInformation;
			synchronized (channelSelectorMap) {
				selectorInformation = channelSelectorMap.remove(socketChannel);
			}

			socketChannel.keyFor(selectorInformation.selector()).cancel();
			int newWorkload = selectorInformation.getWorkload() - 1;
			if (newWorkload == 0) {
				stop(selectorInformation);
			} else {
				selectorInformation.setWorkload(newWorkload);
			}
			printCurrentStand();
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
			lock.unlock();
		}
	}

	private void printCurrentStand() {
		System.out.println("\n\n\nElements stored: " + countConnectNodes() + "(" + countReceivingSelectors() + ")\n\n\n");
	}

	public void setMaxWorkload(int to) {
		this.maxWorkload = to;
	}

	public int countReceivingSelectors() {
		synchronized (storedInformation) {
			return storedInformation.size();
		}
	}

	public int countConnectNodes() {
		synchronized (channelSelectorMap) {
			return channelSelectorMap.size();
		}
	}

	private void stop(SelectorInformation selector) {
		selector.getReceiveObjectListener().stop();
		synchronized (storedInformation) {
			storedInformation.remove(selector);
		}
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
		synchronized (channelSelectorMap) {
			channelSelectorMap.put(socketChannel, selectorInformation);
		}
		selectorInformation.incrementWorkload();
	}

	private boolean isCurrentUsable() throws IOException {
		return pointer.get() != null && pointer.get().getWorkload() < maxWorkload;
	}

	private void setNew(SelectorInformation old) throws IOException {
		if (old != null) {
			synchronized (storedInformation) {
				storedInformation.add(old);
			}
		}

		SelectorInformation usable = getOtherUsable();
		if (usable != null) {
			pointer.set(usable);
		} else {
			Selector selector = Selector.open();
			final SelectorInformation information = new SelectorInformation(selector, createReceiveObjectListener(selector));
			pointer.set(information);
			synchronized (storedInformation) {
				storedInformation.add(information);
			}
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
