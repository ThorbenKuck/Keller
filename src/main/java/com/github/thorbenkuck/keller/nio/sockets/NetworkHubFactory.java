package com.github.thorbenkuck.keller.nio.sockets;

import com.github.thorbenkuck.keller.annotations.Experimental;

import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

public interface NetworkHubFactory {

	static NetworkHubFactory create() {
		return new LocalNetworkHubFactory();
	}

	NetworkHubFactory ifObjectArrives(Consumer<Message> consumer);

	NetworkHubFactory ifClientConnects(Consumer<SocketChannel> consumer);

	@Experimental
	NetworkHubFactory onDisconnect(Consumer<SocketChannel> consumer);

	NetworkHubFactory setExecutorService(ExecutorService executorService);

	NetworkHubFactory setBufferSize(int bufferSize);

	NetworkHubFactory serializer(Function<Object, String> function);

	NetworkHubFactory deserializer(Function<String, Object> function);

	NetworkHub build();
}
