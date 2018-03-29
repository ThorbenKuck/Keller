package com.github.thorbenkuck.keller.nio.sockets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.function.Consumer;
import java.util.function.Function;

public interface NetworkWorker {
	void initialize(String string, int port) throws IOException;

	void initialize(InetSocketAddress inetSocketAddress) throws IOException;

	void send(Object object) throws IOException;

	void addReceivedListener(Consumer<Message> consumer);

	void setSerializer(Function<Object, String> function);

	void close() throws IOException;

	void setDeSerializer(Function<String, Object> deSerializer);
}
