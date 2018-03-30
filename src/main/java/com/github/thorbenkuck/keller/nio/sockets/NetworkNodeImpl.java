package com.github.thorbenkuck.keller.nio.sockets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

	@Override
	public void initialize(final String string, int port) throws IOException {
		initialize(new InetSocketAddress(string, port));
	}

	@Override
	public void initialize(final InetSocketAddress inetSocketAddress) throws IOException {
		channel = SocketChannel.open(inetSocketAddress);
		channel.configureBlocking(false);
		final Selector selector = Selector.open();
		channel.register(selector, SelectionKey.OP_READ, null);	executorService.submit(new Worker(selector, disconnectedListener, receivedListener, this::getBufferSize, this::getExecutorService));
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
		executorService.shutdownNow();
		channel.close();
	}

	@Override
	public void setDeSerializer(Function<String, Object> deSerializer) {
		this.deSerializer.setDeserializer(deSerializer);
	}
}