package com.github.thorbenkuck.keller.nio.sockets;

import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

public interface NetworkFactory<T, U> {

	U onObjectReceive(final Consumer<Message> consumer);

	U onDisconnect(final Consumer<SocketChannel> consumer);

	U serializer(final Function<Object, String> function);

	U deserializer(final Function<String, Object> function);

	U executorService(final ExecutorService executorService);

	U onException(final Consumer<Exception> ex);

	U bufferSize(final int bufferSize);

	T build();

}
