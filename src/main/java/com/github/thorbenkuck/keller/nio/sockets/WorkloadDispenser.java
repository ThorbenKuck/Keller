package com.github.thorbenkuck.keller.nio.sockets;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;

public interface WorkloadDispenser extends Iterable<SelectorChannel> {

	boolean isEmpty();

	void assignLowestSelectorChannel();

	void clearAll();

	void cleanUpSelectorChannels();

	void appeal(SocketChannel socketChannel) throws IOException;

	void remove(SocketChannel socketChannel);

	void setMaxWorkload(int to);

	int countSelectorChannels();

	int countConnectNodes();

	List<SocketChannel> collectCorpses();

	List<SocketChannel> deepCollectCorpses();

	void shutdown();
}
