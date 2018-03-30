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
	private final WorkloadDispenser workloadDispenser = new WorkloadDispenser(disconnectedListener, receivedListener, deserializer);
	private NewConnectionListener connectionListener;
	private Selector selector;
	private ExecutorService executorService = Executors.newCachedThreadPool();
	private int bufferSize = 256;
	private ServerSocketChannel channel;

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
		selector = Selector.open();
		channel.register(selector, SelectionKey.OP_ACCEPT, null);
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
		connectionListener = new NewConnectionListener(selector, connectedListener, channel);
		executorService.submit(connectionListener);
	}

	private int getBufferSize() {
		return bufferSize;
	}

	private ExecutorService getExecutorService() {
		return executorService;
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
	public void addReceivedListener(final Consumer<Message> consumer) {
		this.receivedListener.add(consumer);
	}

	@Override
	public void addConnectedListener(final Consumer<SocketChannel> consumer) {
		this.connectedListener.add(consumer);
	}

	@Override
	public void addDisconnectedListener(final Consumer<SocketChannel> channelConsumer) {
		this.disconnectedListener.add(channelConsumer);
	}

	@Override
	public void setDeserializer(Function<String, Object> deserializer) {
		this.deserializer.setDeserializer(deserializer);
	}

	@Override
	public void setSerializer(Function<Object, String> function) {
		sender.setSerializer(function);
	}

	@Override
	public void send(Object object, SocketChannel socketChannel) throws IOException {
		sender.send(object, socketChannel);
	}

	@Override
	public void close() throws IOException {
		System.out.println("Close requested");
		executorService.shutdown();
		connectionListener.stop();
		selector.wakeup();
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
		channel.keyFor(selector).cancel();
		selector.close();
	}

	@Override
	public boolean isOpen() {
		return channel.isOpen();
	}

	@Override
	public int countReceivingSelectors() {
		return workloadDispenser.countReceivingSelectors();
	}

	@Override
	public int countConnectNodes() {
		return workloadDispenser.countConnectNodes();
	}
}
