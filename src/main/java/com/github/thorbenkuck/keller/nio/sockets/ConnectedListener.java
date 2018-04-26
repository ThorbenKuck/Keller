package com.github.thorbenkuck.keller.nio.sockets;

import com.github.thorbenkuck.keller.pipe.Pipeline;

import java.nio.channels.SocketChannel;
import java.util.function.Consumer;

final class ConnectedListener {

	private final Pipeline<SocketChannel> connectedPipeline = Pipeline.unifiedCreation();

	public final void handle(SocketChannel socketChannel) {
		connectedPipeline.apply(socketChannel);
	}

	public final void add(Consumer<SocketChannel> channelConsumer) {
		connectedPipeline.addFirst(channelConsumer);
	}

	public final void addLast(Consumer<SocketChannel> channelConsumer) {
		connectedPipeline.addFirst(channelConsumer);
	}

}
