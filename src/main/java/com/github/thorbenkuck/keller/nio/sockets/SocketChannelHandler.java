package com.github.thorbenkuck.keller.nio.sockets;

import java.nio.channels.SocketChannel;

public interface SocketChannelHandler {

	void newSocketConnected(SocketChannel socketChannel);

	void received(Object object, SocketChannel socketChannel);

	void disconnected(SocketChannel socketChannel);

}
