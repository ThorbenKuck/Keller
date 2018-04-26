package com.github.thorbenkuck.keller.nio.sockets;

import com.github.thorbenkuck.keller.pipe.Pipeline;

import java.nio.channels.SocketChannel;
import java.util.function.Consumer;

final class DisconnectedListener {

	private final Pipeline<SocketChannel> messagePipeline = Pipeline.unifiedCreation();

	public final void handle(SocketChannel channel) {
		try {
			messagePipeline.apply(channel);
		} catch (Throwable t) {
			t.printStackTrace(System.out);
		}
	}

	public final void add(Consumer<SocketChannel> consumer) {
		messagePipeline.addLast(consumer);
	}

	public final void addFirst(Consumer<SocketChannel> consumer) {
		messagePipeline.addFirst(consumer);
	}
}
