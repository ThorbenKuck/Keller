package com.github.thorbenkuck.keller.nio.sockets;

import java.nio.channels.SocketChannel;

public final class Message {

	private final Object content;
	private final SocketChannel channel;

	public Message(Object content, SocketChannel channel) {
		this.content = content;
		this.channel = channel;
	}

	public final Object getContent() {
		return content;
	}

	public final SocketChannel getChannel() {
		return channel;
	}
}
