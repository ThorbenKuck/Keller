package com.github.thorbenkuck.keller.nio.sockets;

import com.github.thorbenkuck.keller.annotations.Experimental;

import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

class LocalNetworkHubFactory implements NetworkHubFactory {

	private NetworkHubImpl networkListener = new NetworkHubImpl();

	@Override
	public NetworkHubFactory ifObjectArrives(Consumer<Message> consumer) {
		networkListener.addReceivedListener(consumer);

		return this;
	}

	@Override
	public NetworkHubFactory ifClientConnects(Consumer<SocketChannel> consumer) {
		networkListener.addConnectedListener(consumer);

		return this;
	}

	@Experimental
	@Override
	public NetworkHubFactory onDisconnect(Consumer<SocketChannel> consumer) {
		networkListener.addDisconnectedListener(consumer);

		return this;
	}

	@Override
	public NetworkHubFactory setExecutorService(ExecutorService executorService) {
		networkListener.setExecutorService(executorService);

		return this;
	}

	@Override
	public NetworkHubFactory setBufferSize(int bufferSize) {
		networkListener.setBufferSize(bufferSize);

		return this;
	}

	@Override
	public NetworkHubFactory serializer(Function<Object, String> function) {
		networkListener.setSerializer(function);

		return this;
	}

	@Override
	public NetworkHubFactory deserializer(Function<String, Object> function) {
		networkListener.setDeserializer(function);

		return this;
	}

	@Override
	public NetworkHub build() {
		return networkListener;
	}
}
