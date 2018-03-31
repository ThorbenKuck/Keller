package com.github.thorbenkuck.keller.nio.sockets;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;

public interface WorkloadDispenser extends Iterable<SelectorChannel> {
	void appeal(SocketChannel socketChannel) throws IOException;

	List<SocketChannel> collectCorpses();

	List<SocketChannel> deepCollectCorpses();

	void clearAll();

	void remove(SocketChannel socketChannel);

	void setMaxWorkload(int to);

	int countSelectorChannels();

	int countConnectNodes();
}
