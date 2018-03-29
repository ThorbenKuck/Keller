package com.github.thorbenkuck.keller.nio.sockets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

public class ReactiveNIONetworkListener {

	private final ConnectedListener connectedListener = new ConnectedListener();
	private final ReceivedListener receivedListener = new ReceivedListener();
	private final DisconnectedListener disconnectedListener = new DisconnectedListener();
	private final Sender sender = new Sender();
	private final Deserializer deserializer = new Deserializer();
	private ExecutorService executorService = Executors.newCachedThreadPool();
	private int bufferSize = 256;
	private ServerSocketChannel channel;

	public void initialize(final int port) throws IOException {
		initialize("localhost", port);
	}

	public void initialize(final String string, int port) throws IOException {
		initialize(new InetSocketAddress(string, port));
	}

	public void initialize(final InetSocketAddress inetSocketAddress) throws IOException {
		channel = ServerSocketChannel.open();
		channel.configureBlocking(false);
		channel.bind(inetSocketAddress);
		final Selector selector = Selector.open();
		final Listener listener = new Listener(selector, channel, receivedListener, connectedListener, disconnectedListener, this::getExecutorService, this::getBufferSize, deserializer);
		executorService.submit(listener);
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

	public void addReceivedListener(final Consumer<Message> consumer) {
		this.receivedListener.add(consumer);
	}

	public void addConnectedListener(final Consumer<SocketChannel> consumer) {
		this.connectedListener.add(consumer);
	}

	public void addDisconnectedListener(final Consumer<SocketChannel> channelConsumer) {
		this.disconnectedListener.add(channelConsumer);
	}

	public void setDeserializer(Function<String, Object> deserializer) {
		this.deserializer.setDeserializer(deserializer);
	}

	public void setSerializer(Function<Object, String> function) {
		sender.setSerializer(function);
	}

	public void send(Object object, SocketChannel socketChannel) throws IOException {
		sender.send(object, socketChannel);
	}
}
