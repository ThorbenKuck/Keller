package com.github.thorbenkuck.keller.nio.sockets;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

public interface WorkloadDispenser extends Iterable<SelectorChannel> {

	void setChoosingStrategy(ChoosingStrategy choosingStrategy);

	void reChoosePointer() throws IOException;

	SelectorChannel forceCreationOfNewSelectorChannel() throws IOException;

	void setExecutorService(ExecutorService executorService);

	int countSelectorChannels();

	int countConnectNodes();

	boolean isEmpty();

	List<SocketChannel> collectCorpses();

	List<SelectorChannel> clearEmpty();

	Optional<SelectorChannel> get(int index);

	void drainAndReassign() throws IOException;

	void clear();

	void shutdown();

	void appeal(SocketChannel socketChannel) throws IOException;

	void remove(SocketChannel socketChannel) throws IOException;

	void setMaxWorkload(int to);

	List<ReadOnlySelectorChannelInformation> dumpInformation();
}
