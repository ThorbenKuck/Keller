package com.github.thorbenkuck.keller.nio.sockets;

import com.github.thorbenkuck.keller.annotations.Experimental;

import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

class LocalNetworkNodeFactory implements NetworkNodeFactory {

	private NetworkNodeImpl networkWorker = new NetworkNodeImpl();

	@Override
	public NetworkNodeFactory ifObjectArrives(Consumer<Message> consumer) {
		networkWorker.addReceivedListener(consumer);

		return this;
	}

	@Experimental
	@Override
	public NetworkNodeFactory onDisconnect(Consumer<SocketChannel> consumer) {
		// There is currently
		// No way, to detect
		// this.. Who the hell
		// designed this API?!
		return this;
	}

	@Override
	public NetworkNodeFactory setExecutorService(ExecutorService executorService) {
		networkWorker.setExecutorService(executorService);

		return this;
	}

	@Override
	public NetworkNodeFactory setBufferSize(int bufferSize) {
		networkWorker.setBufferSize(bufferSize);

		return this;
	}

	@Override
	public NetworkNodeFactory serializer(Function<Object, String> function) {
		networkWorker.setSerializer(function);

		return this;
	}

	@Override
	public NetworkNodeFactory deserializer(Function<String, Object> function) {
		networkWorker.setDeSerializer(function);

		return this;
	}

	@Override
	public NetworkNode build() {
		return networkWorker;
	}
}
