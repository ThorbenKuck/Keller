package com.github.thorbenkuck.keller.nio.sockets;

import java.nio.channels.SocketChannel;

public interface Message {
	Object getContent();

	SocketChannel getChannel();
}
