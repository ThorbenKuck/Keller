package com.github.thorbenkuck.keller.nio.sockets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

class NetworkHubImpl implements NetworkHub {

	private final ConnectedListener connectedListener = new ConnectedListener();
	private final ReceivedListener receivedListener = new ReceivedListener();
	private final DisconnectedListener disconnectedListener = new DisconnectedListener();
	private final Sender sender = new Sender();
	private final Deserializer deserializer = new Deserializer();
	private final Dispenser workloadDispenser = new Dispenser(disconnectedListener, receivedListener, deserializer, this::consume);
	private Consumer<Exception> onException = e -> e.printStackTrace(System.out);
	private NewConnectionListener connectionListener;
	private Selector connectionSelector;
	private ExecutorService executorService = Executors.newCachedThreadPool();
	private int bufferSize = 256;
	private ServerSocketChannel channel;

	private int getBufferSize() {
		return bufferSize;
	}

	private ExecutorService getExecutorService() {
		return executorService;
	}

	private void consume(Exception e) {
		onException.accept(e);
	}

	void setOnException(Consumer<Exception> exceptionConsumer){
		this.onException = exceptionConsumer;
	}

	void setExecutorService(final ExecutorService executorService) {
		this.executorService = executorService;
	}

	void setBufferSize(final int bufferSize) {
		this.bufferSize = bufferSize;
	}

	void setMaxWorkloadPerSelector(final int workload) {
		workloadDispenser.setMaxWorkload(workload);
	}

	@Override
	public void open(final int port) throws IOException {
		open("localhost", port);
	}

	@Override
	public void open(final String string, int port) throws IOException {
		open(new InetSocketAddress(string, port));
	}

	@Override
	public void open(final InetSocketAddress inetSocketAddress) throws IOException {
		workloadDispenser.setBufferSize(this::getBufferSize);
		workloadDispenser.setExecutorServiceSupplier(this::getExecutorService);
		channel = ServerSocketChannel.open();
		channel.configureBlocking(false);
		channel.bind(inetSocketAddress);
		connectionSelector = Selector.open();
		channel.register(connectionSelector, SelectionKey.OP_ACCEPT, null);
		connectedListener.addFirst(connected -> {
			try {
				workloadDispenser.appeal(connected);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		disconnectedListener.add(socketChannel -> {
			workloadDispenser.remove(socketChannel);
			try {
				socketChannel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		connectionListener = new NewConnectionListener(connectionSelector, connectedListener, channel);
		executorService.submit(connectionListener);
	}

	@Override
	public void addReceivedListener(final Consumer<Message> consumer) {
		if(!isOpen()) {
			return;
		}
		this.receivedListener.add(consumer);
	}

	@Override
	public void addConnectedListener(final Consumer<SocketChannel> consumer) {
		if(!isOpen()) {
			return;
		}
		this.connectedListener.add(consumer);
	}

	@Override
	public void addDisconnectedListener(final Consumer<SocketChannel> channelConsumer) {
		if(!isOpen()) {
			return;
		}
		this.disconnectedListener.add(channelConsumer);
	}

	@Override
	public void setDeserializer(Function<String, Object> deserializer) {
		if(!isOpen()) {
			return;
		}
		this.deserializer.setDeserializer(deserializer);
	}

	@Override
	public void setSerializer(Function<Object, String> function) {
		if(!isOpen()) {
			return;
		}
		sender.setSerializer(function);
	}

	@Override
	public void send(Object object, SocketChannel socketChannel) throws IOException {
		if(!isOpen()) {
			return;
		}
		try {
			sender.send(object, socketChannel);
		} catch (ClosedChannelException c) {
			disconnectedListener.handle(socketChannel);
			throw c;
		}
	}

	@Override
	public synchronized void close() throws IOException {
		if(!isOpen()) {
			return;
		}
		System.out.println("Close requested");
		executorService.shutdown();
		connectionListener.stop();
		workloadDispenser.shutdown();
		connectionSelector.wakeup();
		try {
			executorService.awaitTermination(500, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(!executorService.isShutdown()) {
			System.out.println("Executor forcefully shutdown requested now!");
			executorService.shutdownNow();
		}
		channel.close();
		channel.keyFor(connectionSelector).cancel();
		connectionSelector.close();
	}

	@Override
	public WorkloadDispenser workloadDispenser() {
		return workloadDispenser;
	}

	@Override
	public boolean isOpen() {
		return channel == null || channel.isOpen();
	}
}
