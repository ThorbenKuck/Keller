package com.github.thorbenkuck.keller.nio.sockets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

final class NativeNetworkNode implements NetworkNode {

	private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
	private final Sender sender = new Sender();
	private int bufferSize = 256;
	private SocketChannel channel;
	private final ReceivedListener receivedListener = new ReceivedListener();
	private final DisconnectedListener disconnectedListener = new DisconnectedListener();
	private final Deserializer deSerializer = new Deserializer();
	private Selector selector;
	private ReceiveObjectListener worker;
	private Consumer<Exception> exceptionConsumer = e -> e.printStackTrace(System.out);

	private void handleDisconnect(SocketChannel channel) {
		try {
			channel.close();
			selector.close();
			worker.stop();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int getBufferSize() {
		return bufferSize;
	}

	private ExecutorService getExecutorService() {
		return executorService;
	}

	private void consume(final Exception e) {
		exceptionConsumer.accept(e);
	}

	final void setExecutorService(final ExecutorService executorService) {
		this.executorService = executorService;
	}

	final void setBufferSize(final int bufferSize) {
		this.bufferSize = bufferSize;
	}

	final void setExceptionConsumer(final Consumer<Exception> consumer) {
		this.exceptionConsumer = consumer;
	}

	@Override
	public final void open(final int port) throws IOException {
		open("localhost", port);
	}

	@Override
	public final void open(final String string, final int port) throws IOException {
		open(new InetSocketAddress(string, port));
	}

	@Override
	public final void open(final InetSocketAddress inetSocketAddress) throws IOException {
		channel = SocketChannel.open(inetSocketAddress);
		channel.configureBlocking(false);
		selector = Selector.open();
		channel.register(selector, SelectionKey.OP_READ, null);
		disconnectedListener.addFirst(this::handleDisconnect);
		worker = new ReceiveObjectListener(selector, disconnectedListener, receivedListener, deSerializer, this::getBufferSize, this::getExecutorService, this::consume);
		executorService.submit(worker);
	}

	@Override
	public final void send(final Object object) throws IOException {
		if(!isOpen()) {
			return;
		}
		try {
			sender.send(object, channel);
		} catch (ClosedChannelException c) {
			disconnectedListener.handle(channel);
			throw c;
		}
	}

	@Override
	public final void addReceivedListener(final Consumer<Message> consumer) {
		this.receivedListener.add(consumer);
	}

	@Override
	public final void addDisconnectedListener(final Consumer<SocketChannel> consumer) {
		this.disconnectedListener.add(consumer);
	}

	@Override
	public final void setSerializer(final Function<Object, String> function) {
		sender.setSerializer(function);
	}

	@Override
	public final void close() throws IOException {
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
	public final void setDeSerializer(final Function<String, Object> deSerializer) {
		this.deSerializer.setDeserializer(deSerializer);
	}

	@Override
	public final boolean isOpen() {
		return channel.isOpen();
	}

	@Override
	public final SocketChannel getChannel() {
		return channel;
	}
}
