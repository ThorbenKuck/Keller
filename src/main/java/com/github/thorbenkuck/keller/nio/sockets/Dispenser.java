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

class Dispenser implements WorkloadDispenser {

	private final Set<SelectorInformation> storedInformation = new HashSet<>();
	private final Map<SocketChannel, SelectorInformation> channelSelectorMap = new HashMap<>();
	private final AtomicReference<SelectorInformation> pointer = new AtomicReference<>();
	private final DisconnectedListener disconnectedListener;
	private final ReceivedListener receivedListener;
	private final Deserializer deserializer;
	private final Lock mutex = new ReentrantLock(true);
	private Supplier<Integer> bufferSize;
	private Supplier<ExecutorService> executorServiceSupplier;
	private int maxWorkload;

	Dispenser(DisconnectedListener disconnectedListener, ReceivedListener receivedListener, Deserializer deserializer) {
		this(disconnectedListener, receivedListener, deserializer, 1024);
	}

	Dispenser(DisconnectedListener disconnectedListener, ReceivedListener receivedListener, Deserializer deserializer, int maxWorkload) {
		this.disconnectedListener = disconnectedListener;
		this.receivedListener = receivedListener;
		this.deserializer = deserializer;
		this.maxWorkload = maxWorkload;
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

	private void printCurrentStand() {
		System.out.println("\n\n\nElements stored: " + countConnectNodes() + "(" + countReceivingSelectors() + ")\n\n\n");
	}

	private void clearFrom(SocketChannel socketChannel, SelectorInformation selectorInformation) {
		socketChannel.keyFor(selectorInformation.selector()).cancel();
		int newWorkload = selectorInformation.getWorkload() - 1;
		if (newWorkload == 0) {
			stop(selectorInformation);
		} else {
			selectorInformation.setWorkload(newWorkload);
		}
		printCurrentStand();
	}

	private void appealAll(Collection<SocketChannel> channels) throws IOException {
		IOException exception = null;
		for(SocketChannel socketChannel : channels) {
			try {
				appeal(socketChannel);
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

	private void unHookAll(Collection<SocketChannel> informationCollection) {
		for(SocketChannel socketChannel : informationCollection) {
			remove(socketChannel);
		}
	}

	@Override
	public void appeal(SocketChannel socketChannel) throws IOException {
		try {
			mutex.lock();
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
			// catch a rouge Exception this way
			throwable.printStackTrace(System.out);
		} finally {
			mutex.unlock();
		}
	}

	@Override
	public List<SocketChannel> collectCorpses() {
		System.out.println("\n\nTrying to collect corpses ..\n\n");
		final List<SocketChannel> socketChannels = new ArrayList<>();
		try {
			final Set<SocketChannel> keySet;
			synchronized (channelSelectorMap) {
				if(channelSelectorMap.isEmpty()) {
					return socketChannels;
				}
				keySet = channelSelectorMap.keySet();
			}
			mutex.lock();
			for (SocketChannel socketChannel : keySet) {
				System.out.println("Looking at: " + socketChannel);
				if (!socketChannel.isConnected() || !socketChannel.isOpen()) {
					System.err.println("Found corpse");
					SelectorInformation information;
					synchronized (channelSelectorMap) {
						information = channelSelectorMap.get(socketChannel);
					}
					if(information == null) {
						throw new ConcurrentModificationException();
					}
					socketChannels.add(socketChannel);
					clearFrom(socketChannel, information);
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
		}
		return socketChannels;
	}

	@Override
	public void redistributeSelectors() throws IOException {
		final List<SocketChannel> toWorkOn = new ArrayList<>();
		try {
			mutex.lock();
			final Set<SocketChannel> keySet;
			synchronized (channelSelectorMap) {
				if(channelSelectorMap.isEmpty()) {
					return;
				}
				keySet = channelSelectorMap.keySet();
			}

			for(SocketChannel socketChannel : keySet) {
				SelectorInformation information;
				synchronized (channelSelectorMap) {
					information = channelSelectorMap.get(socketChannel);
				}
				if(information == null) {
					throw new ConcurrentModificationException();
				}
				if(information.getWorkload() < maxWorkload) {
					continue;
				}
				toWorkOn.add(socketChannel);
			}
			if(toWorkOn.isEmpty()) {
				return;
			}
			System.out.println("Will now unhook " + toWorkOn);
			unHookAll(toWorkOn);
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
		}
		appealAll(toWorkOn);
	}

	@Override
	public void remove(SocketChannel socketChannel) {
		try {
			mutex.lock();
			SelectorInformation selectorInformation;
			synchronized (channelSelectorMap) {
				if(channelSelectorMap.isEmpty()) {
					return;
				}
				selectorInformation = channelSelectorMap.remove(socketChannel);
			}
			clearFrom(socketChannel, selectorInformation);
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
		}
	}

	@Override
	public void setMaxWorkload(int to) {
		this.maxWorkload = to;
	}

	@Override
	public int countReceivingSelectors() {
		synchronized (storedInformation) {
			return storedInformation.size();
		}
	}

	@Override
	public int countConnectNodes() {
		synchronized (channelSelectorMap) {
			return channelSelectorMap.size();
		}
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
