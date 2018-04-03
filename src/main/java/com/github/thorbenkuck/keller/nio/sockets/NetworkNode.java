package com.github.thorbenkuck.keller.nio.sockets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.function.Consumer;
import java.util.function.Function;

public interface NetworkNode {

	void open(String string, int port) throws IOException;

	void open(InetSocketAddress inetSocketAddress) throws IOException;

	void send(Object object) throws IOException;

	void addReceivedListener(Consumer<Message> consumer);

	void addDisconnectedListener(Consumer<SocketChannel> consumer);

	void setSerializer(Function<Object, String> function);

	void close() throws IOException;

	void setDeSerializer(Function<String, Object> deSerializer);

	boolean isOpen();
}
