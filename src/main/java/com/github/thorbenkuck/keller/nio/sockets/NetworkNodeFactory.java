package com.github.thorbenkuck.keller.nio.sockets;

import com.github.thorbenkuck.keller.annotations.Experimental;

import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

public interface NetworkNodeFactory {

	static NetworkNodeFactory create() {
		return new LocalNetworkNodeFactory();
	}

	NetworkNodeFactory ifObjectArrives(Consumer<Message> consumer);

	@Experimental
	NetworkNodeFactory onDisconnect(Consumer<SocketChannel> consumer);

	NetworkNodeFactory setExecutorService(ExecutorService executorService);

	NetworkNodeFactory setBufferSize(int bufferSize);

	NetworkNodeFactory serializer(Function<Object, String> function);

	NetworkNodeFactory deserializer(Function<String, Object> function);

	NetworkNode build();
}