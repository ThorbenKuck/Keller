package com.github.thorbenkuck.keller.nio.sockets;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;

public interface WorkloadDispenser extends Iterable<SelectorChannel> {

	int countSelectorChannels();

	int countConnectNodes();

	boolean isEmpty();

	List<SocketChannel> collectCorpses();

	List<SocketChannel> deepCollectCorpses();

	List<SelectorChannel> clearEmpty();

	void clearAll();

	void cleanUpSelectorChannels();

	void assignLowestSelectorChannel();

	void shutdown();

	void appeal(SocketChannel socketChannel) throws IOException;

	void remove(SocketChannel socketChannel);

	void setMaxWorkload(int to);
}
