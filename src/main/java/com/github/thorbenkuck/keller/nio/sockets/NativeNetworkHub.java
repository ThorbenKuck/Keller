package com.github.thorbenkuck.keller.nio.sockets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

final class NativeNetworkHub implements NetworkHub {

	private final ConnectedListener connectedListener = new ConnectedListener();
	private final ReceivedListener receivedListener = new ReceivedListener();
	private final DisconnectedListener disconnectedListener = new DisconnectedListener();
	private final Sender sender = new Sender();
	private final Deserializer deserializer = new Deserializer();
	private final Dispenser workloadDispenser = new Dispenser(disconnectedListener, receivedListener, deserializer, this::consume, sender);
	private Consumer<Exception> onException = e -> e.printStackTrace(System.out);
	private NewConnectionListener connectionListener;
	private Selector connectionSelector;
	private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
	private int bufferSize = 256;
	private ServerSocketChannel channel;

	private int getBufferSize() {
		return bufferSize;
	}

	private ExecutorService getExecutorService() {
		return executorService;
	}

	private void consume(final Exception e) {
		onException.accept(e);
	}

	final void setOnException(final Consumer<Exception> exceptionConsumer){
		this.onException = exceptionConsumer;
	}

	final void setExecutorService(final ExecutorService executorService) {
		this.executorService = executorService;
	}

	final void setBufferSize(final int bufferSize) {
		if(bufferSize < 1) {
			throw new IllegalArgumentException("Buffer size must be 1 or greater");
		}
		this.bufferSize = bufferSize;
	}

	final void setMaxWorkloadPerSelector(final int workload) {
		workloadDispenser.setMaxWorkload(workload);
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
		workloadDispenser.setBufferSize(this::getBufferSize);
		workloadDispenser.setExecutorServiceSupplier(this::getExecutorService);
		channel = ServerSocketChannel.open();
		channel.configureBlocking(false);
		channel.bind(inetSocketAddress);
		connectionSelector = Selector.open();
		channel.register(connectionSelector, SelectionKey.OP_ACCEPT, null);
		connectedListener.addLast(connected -> {
			try {
				workloadDispenser.appeal(connected);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		disconnectedListener.add(socketChannel -> {
			try {
				workloadDispenser.remove(socketChannel);
				socketChannel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		connectionListener = new NewConnectionListener(connectionSelector, connectedListener, channel);
		executorService.submit(connectionListener);
	}

	@Override
	public final ServerSocketChannel getChannel() {
		return channel;
	}

	@Override
	public final void addReceivedListener(final Consumer<Message> consumer) {
		if(!isOpen()) {
			return;
		}
		this.receivedListener.add(consumer);
	}

	@Override
	public final void addConnectedListener(final Consumer<SocketChannel> consumer) {
		if(!isOpen()) {
			return;
		}
		this.connectedListener.add(consumer);
	}

	@Override
	public final void addDisconnectedListener(final Consumer<SocketChannel> channelConsumer) {
		if(!isOpen()) {
			return;
		}
		this.disconnectedListener.add(channelConsumer);
	}

	@Override
	public final void setDeserializer(final Function<String, Object> deserializer) {
		if(!isOpen()) {
			return;
		}
		this.deserializer.setDeserializer(deserializer);
	}

	@Override
	public final void setSerializer(final Function<Object, String> function) {
		if(!isOpen()) {
			return;
		}
		sender.setSerializer(function);
	}

	@Override
	public final void send(final Object object, final SocketChannel socketChannel) throws IOException {
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
	public final synchronized void close() throws IOException {
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
	public final WorkloadDispenser workloadDispenser() {
		return workloadDispenser;
	}

	@Override
	public final boolean isOpen() {
		return channel == null || channel.isOpen();
	}
}
