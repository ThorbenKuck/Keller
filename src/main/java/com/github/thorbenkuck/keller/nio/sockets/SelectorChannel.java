package com.github.thorbenkuck.keller.nio.sockets;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.List;

public interface SelectorChannel extends Iterable<SocketChannel> {

	void add(SocketChannel socketChannel) throws ClosedChannelException;

	int getWorkload();

	boolean workloadReached(int maxWorkload);

	Selector selector();

	boolean contains(SocketChannel socketChannel);

	boolean isOpen();

	void remove(SocketChannel socketChannel);

	void wakeup();

	List<SocketChannel> drainEmpty() throws IOException;

	void close() throws IOException;

	List<SocketChannel> getSocketChannels();

	boolean isEmpty();

	ReadOnlySelectorChannelInformation toInformation(int maxWorkload);
}
