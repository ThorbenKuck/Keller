package com.github.thorbenkuck.keller.nio.sockets;

public interface ReadOnlySelectorChannelInformation {

	int getStoredSocketChannels();

	double workloadPercentage();

	boolean isEmpty();

}
