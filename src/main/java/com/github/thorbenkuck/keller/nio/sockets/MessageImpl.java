package com.github.thorbenkuck.keller.nio.sockets;

import java.nio.channels.SocketChannel;

final class MessageImpl implements Message {

	private final Object content;
	private final SocketChannel channel;

	MessageImpl(Object content, SocketChannel channel) {
		this.content = content;
		this.channel = channel;
	}

	@Override
	public final Object getContent() {
		return content;
	}

	@Override
	public final SocketChannel getChannel() {
		return channel;
	}
}
