package com.github.thorbenkuck.keller.nio.sockets;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Supplier;

class ReceivedBytesHandler {

	void handle(SocketChannel channel, Consumer<SocketChannel> disconnected, Supplier<Integer> bufferSize,
	            Deserializer deserializer, Queue<Message> messageQueue, Consumer<Exception> onException) {
		if(!channel.isConnected()) {
			disconnected.accept(channel);
			return;
		}

		final StringBuilder resultBuilder = new StringBuilder();
		final ByteBuffer buffer = ByteBuffer.allocate(bufferSize.get());
		try {
			handle(buffer, channel, resultBuilder, disconnected);
		} catch (IOException e) {
			onException.accept(e);
			disconnected.accept(channel);
			return;
		}
		String result = resultBuilder.toString();
		if(result.isEmpty()) {
			return;
		}
		Object object = deserializer.getDeSerializedContent(result);
		if(object != null) {
			MessageImpl message = new MessageImpl(object, channel);
			messageQueue.add(message);
		}
	}

	private void handle(ByteBuffer buffer, SocketChannel channel, StringBuilder resultBuilder, Consumer<SocketChannel> disconnected) throws IOException {
		int read = channel.read(buffer);
		while(read != 0) {
			if(-1 == read) {
				disconnected.accept(channel);
				return;
			} else {
				buffer.flip();
				resultBuilder.append(new String(buffer.array()).trim());
				buffer.clear();
			}
			read = channel.read(buffer);
		}
	}

}
