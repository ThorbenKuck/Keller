package com.github.thorbenkuck.keller.nio.sockets;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Supplier;

class ReceivedBytesHandler {

	void handle(SocketChannel channel, Consumer<SocketChannel> disconnected, Supplier<Integer> bufferSize, Deserializer deserializer, Queue<Message> messageQueue) {
		if(!channel.isConnected()) {
			disconnected.accept(channel);
			return;
		}

		ByteBuffer buffer = ByteBuffer.allocate(bufferSize.get());
		try {
			if(-1 == channel.read(buffer)) {
				disconnected.accept(channel);
				return;
			}
		} catch (IOException e) {
			e.printStackTrace(System.out);
			disconnected.accept(channel);
			return;
		}
		String result = new String(buffer.array()).trim();
		Object object = deserializer.getDeSerializedContent(result);
		MessageImpl message = new MessageImpl(object, channel);
		messageQueue.add(message);
	}

}
