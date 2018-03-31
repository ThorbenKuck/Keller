package com.github.thorbenkuck.keller.nio.sockets;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;

public interface WorkloadDispenser {
	void appeal(SocketChannel socketChannel) throws IOException;

	List<SocketChannel> collectCorpses();

	void remove(SocketChannel socketChannel);

	void setMaxWorkload(int to);

	int countSelectorChannels();

	int countConnectNodes();
}
