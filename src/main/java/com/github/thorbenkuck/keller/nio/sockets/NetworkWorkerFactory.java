package com.github.thorbenkuck.keller.nio.sockets;

import com.github.thorbenkuck.keller.annotations.Experimental;

import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

public interface NetworkWorkerFactory {

	static NetworkWorkerFactory create() {
		return new LocalNetworkWorkerFactory();
	}

	NetworkWorkerFactory ifObjectArrives(Consumer<Message> consumer);

	@Experimental
	NetworkWorkerFactory onDisconnect(Consumer<SocketChannel> consumer);

	NetworkWorkerFactory setExecutorService(ExecutorService executorService);

	NetworkWorkerFactory setBufferSize(int bufferSize);

	NetworkWorkerFactory serializer(Function<Object, String> function);

	NetworkWorkerFactory deserializer(Function<String, Object> function);

	NetworkWorker build();
}