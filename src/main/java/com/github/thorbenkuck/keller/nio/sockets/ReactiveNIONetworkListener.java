package com.github.thorbenkuck.keller.nio.sockets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ReactiveNIONetworkListener {

	private ExecutorService executorService = Executors.newCachedThreadPool();
	private int bufferSize = 256;
	private ServerSocketChannel channel;
	private ConnectedListener connectedListener = new ConnectedListener();
	private ReceivedListener receivedListener = new ReceivedListener();

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
		final Listener listener = new Listener(selector, channel, receivedListener, connectedListener, this::getExecutorService, this::getBufferSize);
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
}
