package com.github.thorbenkuck.keller.nio.sockets;

import java.nio.channels.SocketChannel;

public final class Message {

	private final String content;
	private final SocketChannel channel;

	public Message(String content, SocketChannel channel) {
		this.content = content;
		this.channel = channel;
	}

	public String getContent() {
		return content;
	}

	public SocketChannel getChannel() {
		return channel;
	}
}
