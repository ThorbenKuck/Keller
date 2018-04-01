package com.github.thorbenkuck.keller.nio.sockets;

import com.github.thorbenkuck.keller.pipe.Pipeline;

import java.nio.channels.SocketChannel;
import java.util.function.Consumer;

class ConnectedListener {

	private final Pipeline<SocketChannel> connectedPipeline = Pipeline.unifiedCreation();

	public void handle(SocketChannel socketChannel) {
		connectedPipeline.apply(socketChannel);
	}

	public void add(Consumer<SocketChannel> channelConsumer) {
		connectedPipeline.addFirst(channelConsumer);
	}

	public void addLast(Consumer<SocketChannel> channelConsumer) {
		connectedPipeline.addFirst(channelConsumer);
	}

}
