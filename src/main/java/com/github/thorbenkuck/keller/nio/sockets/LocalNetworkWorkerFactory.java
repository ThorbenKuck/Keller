package com.github.thorbenkuck.keller.nio.sockets;

import com.github.thorbenkuck.keller.annotations.Experimental;

import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

class LocalNetworkWorkerFactory implements NetworkWorkerFactory {

	private NetworkWorkerImpl networkWorker = new NetworkWorkerImpl();

	@Override
	public NetworkWorkerFactory ifObjectArrives(Consumer<Message> consumer) {
		networkWorker.addReceivedListener(consumer);

		return this;
	}

	@Experimental
	@Override
	public NetworkWorkerFactory onDisconnect(Consumer<SocketChannel> consumer) {
		// There is currently
		// No way, to detect
		// this.. Who the hell
		// designed this API?!
		return this;
	}

	@Override
	public NetworkWorkerFactory setExecutorService(ExecutorService executorService) {
		networkWorker.setExecutorService(executorService);

		return this;
	}

	@Override
	public NetworkWorkerFactory setBufferSize(int bufferSize) {
		networkWorker.setBufferSize(bufferSize);

		return this;
	}

	@Override
	public NetworkWorkerFactory serializer(Function<Object, String> function) {
		networkWorker.setSerializer(function);

		return this;
	}

	@Override
	public NetworkWorkerFactory deserializer(Function<String, Object> function) {
		networkWorker.setDeSerializer(function);

		return this;
	}

	@Override
	public NetworkWorker build() {
		return networkWorker;
	}
}
