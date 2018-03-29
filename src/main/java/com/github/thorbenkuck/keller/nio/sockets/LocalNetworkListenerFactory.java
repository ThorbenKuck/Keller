package com.github.thorbenkuck.keller.nio.sockets;

import com.github.thorbenkuck.keller.annotations.Experimental;

import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

class LocalNetworkListenerFactory implements NetworkListenerFactory {

	private NetworkListenerImpl networkListener = new NetworkListenerImpl();

	@Override
	public NetworkListenerFactory ifObjectArrives(Consumer<Message> consumer) {
		networkListener.addReceivedListener(consumer);

		return this;
	}

	@Override
	public NetworkListenerFactory ifClientConnects(Consumer<SocketChannel> consumer) {
		networkListener.addConnectedListener(consumer);

		return this;
	}

	@Experimental
	@Override
	public NetworkListenerFactory onDisconnect(Consumer<SocketChannel> consumer) {
		networkListener.addDisconnectedListener(consumer);

		return this;
	}

	@Override
	public NetworkListenerFactory setExecutorService(ExecutorService executorService) {
		networkListener.setExecutorService(executorService);

		return this;
	}

	@Override
	public NetworkListenerFactory setBufferSize(int bufferSize) {
		networkListener.setBufferSize(bufferSize);

		return this;
	}

	@Override
	public NetworkListenerFactory serializer(Function<Object, String> function) {
		networkListener.setSerializer(function);

		return this;
	}

	@Override
	public NetworkListenerFactory deserializer(Function<String, Object> function) {
		networkListener.setDeserializer(function);

		return this;
	}

	@Override
	public NetworkListener build() {
		return networkListener;
	}
}
