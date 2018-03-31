package com.github.thorbenkuck.keller.nio.sockets;

import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

public interface NetworkFactory<T, U> {

	U onObjectReceive(Consumer<Message> consumer);

	U onDisconnect(Consumer<SocketChannel> consumer);

	U serializer(Function<Object, String> function);

	U deserializer(Function<String, Object> function);

	U executorService(ExecutorService executorService);

	U onException(Consumer<Exception> ex);

	U bufferSize(int bufferSize);

	T build();

}
