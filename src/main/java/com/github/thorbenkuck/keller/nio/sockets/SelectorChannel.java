package com.github.thorbenkuck.keller.nio.sockets;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.List;

public interface SelectorChannel extends Iterable<SocketChannel> {

	void add(SocketChannel socketChannel) throws ClosedChannelException;

	int getWorkload();

	Selector selector();

	boolean contains(SocketChannel socketChannel);

	void remove(SocketChannel socketChannel);

	void wakeup();

	void close() throws IOException;

	List<SocketChannel> getSocketChannels();
}
