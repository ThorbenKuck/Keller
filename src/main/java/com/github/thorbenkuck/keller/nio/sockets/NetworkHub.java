package com.github.thorbenkuck.keller.nio.sockets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.function.Consumer;
import java.util.function.Function;

public interface NetworkHub {
	void open(int port) throws IOException;

	void open(String string, int port) throws IOException;

	void open(InetSocketAddress inetSocketAddress) throws IOException;

	void addReceivedListener(Consumer<Message> consumer);

	void addConnectedListener(Consumer<SocketChannel> consumer);

	void addDisconnectedListener(Consumer<SocketChannel> channelConsumer);

	void setDeserializer(Function<String, Object> deserializer);

	void setSerializer(Function<Object, String> function);

	void send(Object object, SocketChannel socketChannel) throws IOException;

	void close() throws IOException;

	boolean isOpen();

	int countReceivingSelectors();

	int countConnectNodes();
}
