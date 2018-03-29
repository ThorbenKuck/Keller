package com.github.thorbenkuck.keller.nio.sockets;

import com.github.thorbenkuck.keller.annotations.Experimental;

import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

public interface NetworkListenerFactory {

	static NetworkListenerFactory create() {
		return new LocalNetworkListenerFactory();
	}

	NetworkListenerFactory ifObjectArrives(Consumer<Message> consumer);

	NetworkListenerFactory ifClientConnects(Consumer<SocketChannel> consumer);

	@Experimental
	NetworkListenerFactory onDisconnect(Consumer<SocketChannel> consumer);

	NetworkListenerFactory setExecutorService(ExecutorService executorService);

	NetworkListenerFactory setBufferSize(int bufferSize);

	NetworkListenerFactory serializer(Function<Object, String> function);

	NetworkListenerFactory deserializer(Function<String, Object> function);

	NetworkListener build();
}
