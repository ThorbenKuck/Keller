package com.github.thorbenkuck.keller.nio.sockets;

import com.github.thorbenkuck.keller.datatypes.ConcurrentIterator;
import com.github.thorbenkuck.keller.datatypes.interfaces.Value;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

final class Dispenser implements WorkloadDispenser {

	private final List<LocalSelectorChannel> storedLocalSelectorChannels = new ArrayList<>();
	private final AtomicReference<LocalSelectorChannel> pointer = new AtomicReference<>();
	private final DisconnectedListener disconnectedListener;
	private final ReceivedListener receivedListener;
	private final Deserializer deserializer;
	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);
	private final AtomicBoolean active = new AtomicBoolean(false);
	private final Consumer<Exception> exceptionConsumer;
	private final Sender sender;
	private final Value<ChoosingStrategy> choosingStrategy = Value.of(new FirstFitChoosingStrategy());
	private final Value<Supplier<Integer>> bufferSize = Value.empty();
	private final Value<Supplier<ExecutorService>> executorServiceSupplier = Value.empty();
	private final Value<Integer> maxWorkload = Value.empty();

	Dispenser(DisconnectedListener disconnectedListener, ReceivedListener receivedListener, Deserializer deserializer, Consumer<Exception> consumer, Sender sender) {
		this(disconnectedListener, receivedListener, deserializer, consumer, sender, 1024);
	}

	Dispenser(DisconnectedListener disconnectedListener, ReceivedListener receivedListener, Deserializer deserializer, Consumer<Exception> consumer, Sender sender, int maxWorkload) {
		this.disconnectedListener = disconnectedListener;
		this.receivedListener = receivedListener;
		this.deserializer = deserializer;
		this.exceptionConsumer = consumer;
		this.sender = sender;
		set(maxWorkload);
		set(true);
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

	private boolean isActive() {
		synchronized (active) {
			return active.get();
		}
	}

	private void set(int maxWorkload) {
		synchronized (this.maxWorkload) {
			this.maxWorkload.set(maxWorkload);
		}
	}

	private void set(boolean active) {
		synchronized (this.active) {
			this.active.set(active);
		}
	}

	private int maxWorkload() {
		synchronized (maxWorkload) {
			return maxWorkload.get();
		}
	}

	private int getBufferSize() {
		synchronized (bufferSize) {
			return bufferSize.get().get();
		}
	}

	private ExecutorService getExecutorService() {
		synchronized (executorServiceSupplier) {
			return executorServiceSupplier.get().get();
		}
	}

	void setExecutorServiceSupplier(Supplier<ExecutorService> serviceSupplier) {
		synchronized (executorServiceSupplier) {
			executorServiceSupplier.set(serviceSupplier);
		}
	}

	void setBufferSize(Supplier<Integer> size) {
		synchronized (bufferSize) {
			bufferSize.set(size);
		}
	}

	private void setPointer(LocalSelectorChannel channel) {
		synchronized (pointer) {
			pointer.set(channel);
		}
	}

	private void handle(Exception e) {
		exceptionConsumer.accept(e);
	}

	private void startWriting() {
		readWriteLock.writeLock().lock();
	}

	private void stopWriting() {
		readWriteLock.writeLock().unlock();
	}

	private void startReading() {
		readWriteLock.readLock().lock();
	}

	private void stopReading() {
		readWriteLock.readLock().unlock();
	}

	private LocalSelectorChannel createNewSelectorChannel() throws IOException {
		Selector selector = Selector.open();
		ReceiveObjectListener listener = new ReceiveObjectListener(selector, disconnectedListener, receivedListener, deserializer, this::getBufferSize, this::getExecutorService, this::handle);
		LocalSelectorChannel localSelectorChannel = new LocalSelectorChannel(selector, listener);
		getExecutorService().submit(listener);
		return localSelectorChannel;
	}

	private boolean tryAdding(SocketChannel socketChannel, LocalSelectorChannel selectorChannel) {
		if(selectorChannel == null || !selectorChannel.isOpen()) {
			return false;
		}
		if(selectorChannel.workloadReached(maxWorkload())) {
			return false;
		}

		try {
			selectorChannel.add(socketChannel);
			return true;
		} catch (ClosedChannelException e) {
			handle(e);
			return false;
		}
	}

	private void chooseNewPointer() throws IOException {
		if(copySelectorChannels().isEmpty()) {
			add(createNewSelectorChannel());
			chooseNewPointer();
			return;
		}

		final List<LocalSelectorChannel> selectorChannels = copySelectorChannels();
		final List<SelectorChannel> values = new ArrayList<>();
		for(LocalSelectorChannel socketChannels : selectorChannels) {
			if(!socketChannels.workloadReached(maxWorkload())) {
				values.add(socketChannels);
			}
		}

		selectorChannels.clear();
		this.choosingStrategy.get();

		if(choosingStrategy.isEmpty()) {
			throw new IllegalStateException("No set ChoosingStrategy!");
		}

		Function<List<SelectorChannel>, SelectorChannel> strategy = choosingStrategy.get();

		LocalSelectorChannel newPointer = (LocalSelectorChannel) strategy.apply(values);
		if(newPointer == null) {
			newPointer = createNewSelectorChannel();
			add(newPointer);
		}
		setPointer(newPointer);
	}

	private LocalSelectorChannel getFor(SocketChannel socketChannel) {
		final List<LocalSelectorChannel> copy = copySelectorChannels();
		for(LocalSelectorChannel localSelectorChannel : copy) {
			if(localSelectorChannel.contains(socketChannel)) {
				return localSelectorChannel;
			}
		}
		return null;
	}

	private void clearAndCloseAll() {
		List<LocalSelectorChannel> selectorChannels = copySelectorChannels();
		for(LocalSelectorChannel channel : selectorChannels) {
			try {
				channel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			remove(channel);

		}
	}

	private List<LocalSelectorChannel> getEmptySelectorChannels() {
		final List<LocalSelectorChannel> empty = new ArrayList<>();
		final List<LocalSelectorChannel> copy = copySelectorChannels();

		for(LocalSelectorChannel channel : copy) {
			if(channel.isEmpty() || channel.getWorkload() == 0) {
				empty.add(channel);
			}
		}

		copy.clear();
		return empty;
	}

	private void addSocketChannel(SocketChannel socketChannel) throws IOException {
		LocalSelectorChannel pointer = getPointer();
		if(!tryAdding(socketChannel, pointer)) {
			chooseNewPointer();
			pointer  = getPointer();
			if(!tryAdding(socketChannel, pointer)) {
				throw new IOException("Could not choose pointer!");
			}
		}
	}

	@Override
	public void setChoosingStrategy(ChoosingStrategy choosingStrategy) {
		synchronized (this.choosingStrategy) {
			this.choosingStrategy.set(choosingStrategy);
		}
	}

	@Override
	public void reChoosePointer() throws IOException {
		try {
			startWriting();
			chooseNewPointer();
		} finally {
			stopWriting();
		}
	}

	@Override
	public SelectorChannel forceCreationOfNewSelectorChannel() throws IOException {
		try {
			startWriting();
			LocalSelectorChannel newChannel = createNewSelectorChannel();
			add(newChannel);
			return newChannel;
		} finally {
			stopWriting();
		}
	}

	@Override
	public void setExecutorService(ExecutorService executorService) {
		try {
			startWriting();
			setExecutorServiceSupplier(() -> executorService);
		} finally {
			stopWriting();
		}
	}

	@Override
	public int countSelectorChannels() {
		try {
			startReading();
			synchronized (storedLocalSelectorChannels) {
				return storedLocalSelectorChannels.size();
			}
		} finally {
			stopReading();
		}
	}

	@Override
	public int countConnectNodes() {
		try {
			startReading();
			int size = 0;
			final List<LocalSelectorChannel> selectorChannels = copySelectorChannels();
			for (LocalSelectorChannel localSelectorChannel : selectorChannels) {
				size += localSelectorChannel.getWorkload();
			}
			return size;
		} finally {
			stopReading();
		}
	}

	@Override
	public boolean isEmpty() {
		try {
			startReading();
			boolean empty;
			synchronized (storedLocalSelectorChannels) {
				empty = storedLocalSelectorChannels.isEmpty();
			}
			return empty;
		} finally {
			stopReading();
		}
	}

	@Override
	public List<SocketChannel> collectCorpses() {
		try{
			startWriting();
			if(!isActive()) {
				return new ArrayList<>();
			}
			final List<SocketChannel> returnValue = new ArrayList<>();
			final List<LocalSelectorChannel> selectorChannels = copySelectorChannels();

			final List<SocketChannel> channels = new ArrayList<>();
			for(LocalSelectorChannel localSelectorChannel : selectorChannels) {
				channels.addAll(localSelectorChannel.getSocketChannels());
				for(SocketChannel socketChannel : channels) {
					if(!socketChannel.isOpen() || !socketChannel.isConnected()) {
						localSelectorChannel.remove(socketChannel);
						returnValue.add(socketChannel);
					}
				}
				channels.clear();
			}

			return returnValue;
		} finally {
			stopWriting();
			clearEmpty();
		}
	}

	@Override
	public List<SelectorChannel> clearEmpty() {
		try{
			startWriting();
			if(!isActive()) {
				return new ArrayList<>();
			}
			final List<SelectorChannel> returnValue = new ArrayList<>();
			final List<LocalSelectorChannel> empty = getEmptySelectorChannels();
			for(LocalSelectorChannel socketChannels : empty) {
				remove(socketChannels);
				returnValue.add(socketChannels);
			}

			return returnValue;
		} finally {
			stopWriting();
		}
	}

	@Override
	public Optional<SelectorChannel> get(int index) {
		try {
			startReading();
			if(!isActive()) {
				return Optional.empty();
			}
			SelectorChannel socketChannel;
			synchronized (storedLocalSelectorChannels) {
				socketChannel = storedLocalSelectorChannels.get(index);
			}
			return Optional.ofNullable(socketChannel);
		} finally {
			stopReading();
		}
	}

	@Override
	public void drainAndReassign() throws IOException {
		try{
			startWriting();
			if(!isActive()) {
				return;
			}
			final List<SocketChannel> allChannels = new ArrayList<>();
			final List<LocalSelectorChannel> selectorChannels = copySelectorChannels();
			for(LocalSelectorChannel channel : selectorChannels) {
				try {
					allChannels.addAll(channel.drainEmpty());
				} catch (IOException e) {
					handle(e);
				}
			}
			for(SocketChannel socketChannel : allChannels) {
				addSocketChannel(socketChannel);
			}
		} finally {
			stopWriting();
		}
	}

	@Override
	public void clear() {
		try{
			startWriting();
			if(!isActive()) {
				return;
			}
			clearAndCloseAll();
		} finally {
			stopWriting();
		}
	}

	@Override
	public void shutdown() {
		try{
			startWriting();
			if(!isActive()) {
				return;
			}
			set(false);
			clearAndCloseAll();
		} finally {
			stopWriting();
		}
	}

	@Override
	public void appeal(SocketChannel socketChannel) throws IOException {
		try{
			startWriting();
			if(!isActive()) {
				return;
			}
			addSocketChannel(socketChannel);
		} finally {
			stopWriting();
		}
	}

	@Override
	public void remove(SocketChannel socketChannel) throws IOException {
		try{
			startWriting();
			if(!isActive()) {
				return;
			}
			LocalSelectorChannel localSelectorChannel = getFor(socketChannel);
			if(localSelectorChannel == null) {
				return;
			}
			localSelectorChannel.remove(socketChannel);
			if(localSelectorChannel.getWorkload() == 0) {
				localSelectorChannel.close();
				synchronized (storedLocalSelectorChannels) {
					storedLocalSelectorChannels.remove(localSelectorChannel);
				}
			}
		} finally {
			stopWriting();
		}
	}

	@Override
	public void setMaxWorkload(int to) {
		try {
			startWriting();
			if (to == 0 || to < -1) {
				throw new IllegalArgumentException("SelectorChannels need a workload > 0 or -1");
			}
			set(to);
		} finally {
			stopWriting();
		}
	}

	@Override
	public List<ReadOnlySelectorChannelInformation> dumpInformation() {
		try {
			startReading();
			final List<ReadOnlySelectorChannelInformation> value = new ArrayList<>();
			final List<LocalSelectorChannel> copy = copySelectorChannels();
			for(LocalSelectorChannel channel : copy) {
				value.add(channel.toInformation(maxWorkload()));
			}

			return value;
		} finally {
			stopReading();
		}
	}

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
		public boolean workloadReached(int maxWorkload) {
			return maxWorkload != -1 && getWorkload() == maxWorkload;
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
		public List<SocketChannel> drainEmpty() throws IOException {
			final List<SocketChannel> copy;
			synchronized (socketChannels) {
				copy = new ArrayList<>(socketChannels);
			}
			for (SocketChannel socketChannel : copy) {
				remove(socketChannel);
			}

			return copy;
		}

		@Override
		public void close() throws IOException {
			final List<SocketChannel> copy = drainEmpty();
			for (SocketChannel socketChannel : copy) {
				socketChannel.close();
			}
			receiveObjectListener.stop();
			selector.wakeup();
			selector.close();
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

		@Override
		public boolean isEmpty() {
			synchronized (socketChannels) {
				return socketChannels.isEmpty();
			}
		}

		@Override
		public ReadOnlySelectorChannelInformation toInformation(int maxWorkload) {
			int socketChannels = getWorkload();
			double percentage = (double) socketChannels / (double) maxWorkload;
			return new NativeReadOnlySelectorChannelInformation(socketChannels, percentage);
		}
	}

	private final class NativeReadOnlySelectorChannelInformation implements ReadOnlySelectorChannelInformation {

		private final int amountOfSocketChannels;
		private final double workload;

		private NativeReadOnlySelectorChannelInformation(final int amountOfSocketChannels, final double workload) {
			this.amountOfSocketChannels = amountOfSocketChannels;
			this.workload = workload;
		}

		@Override
		public int getStoredSocketChannels() {
			return amountOfSocketChannels;
		}

		@Override
		public double workloadPercentage() {
			return workload;
		}

		@Override
		public boolean isEmpty() {
			return amountOfSocketChannels == 0;
		}

		@Override
		public String toString() {
			return "SelectorChannelInformation{empty=" + isEmpty() + ", " + amountOfSocketChannels + ": " + (workload * 100) + "%}";
		}
	}
}
