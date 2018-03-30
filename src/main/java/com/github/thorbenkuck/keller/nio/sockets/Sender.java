package com.github.thorbenkuck.keller.nio.sockets;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.function.Function;

public class Sender {

	private Function<Object, String> serializer = Object::toString;

	private String serialize(Object o) {
		return serializer.apply(o);
	}

	public void send(final Object object, final SocketChannel channel) throws IOException {
		System.out.println("Sending " + object + " to " + channel.getRemoteAddress());
		String toSend = serialize(object);
		byte[] message = toSend.getBytes();
		ByteBuffer buffer = ByteBuffer.wrap(message);
		channel.write(buffer);

		buffer.clear();
	}

	public void setSerializer(Function<Object, String> serializer) {
		this.serializer = serializer;
	}
}