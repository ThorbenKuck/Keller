package com.github.thorbenkuck.keller.nio.sockets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

class NetworkNodeImpl implements NetworkNode {

	private ExecutorService executorService = Executors.newCachedThreadPool();
	private final Sender sender = new Sender();
	private int bufferSize = 256;
	private SocketChannel channel;
	private final ReceivedListener receivedListener = new ReceivedListener();
	private final DisconnectedListener disconnectedListener = new DisconnectedListener();
	private final Deserializer deSerializer = new Deserializer();
	private Selector selector;
	private Worker worker;

	@Override
	public void open(final String string, int port) throws IOException {
		open(new InetSocketAddress(string, port));
	}

	@Override
	public void open(final InetSocketAddress inetSocketAddress) throws IOException {
		channel = SocketChannel.open(inetSocketAddress);
		channel.configureBlocking(false);
		selector = Selector.open();
		channel.register(selector, SelectionKey.OP_READ, null);
		worker = new Worker(selector, disconnectedListener, receivedListener, deSerializer, this::getBufferSize, this::getExecutorService);
		executorService.submit(worker);
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

	@Override
	public void send(Object object) throws IOException {
		sender.send(object, channel);
	}

	@Override
	public void addReceivedListener(Consumer<Message> consumer) {
		this.receivedListener.add(consumer);
	}

	@Override
	public void setSerializer(Function<Object, String> function) {
		sender.setSerializer(function);
	}

	@Override
	public void close() throws IOException {
		executorService.shutdown();
		worker.stop();
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
	public void setDeSerializer(Function<String, Object> deSerializer) {
		this.deSerializer.setDeserializer(deSerializer);
	}

	@Override
	public boolean isOpen() {
		return channel.isOpen();
	}
}
