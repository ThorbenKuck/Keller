package com.github.thorbenkuck.keller.nio.sockets;

import java.nio.channels.SocketChannel;
import java.util.function.Consumer;

public interface NetworkHubFactory extends NetworkFactory<NetworkHub, NetworkHubFactory> {

	static NetworkHubFactory create() {
		return new LocalNetworkHubFactory();
	}

	NetworkHubFactory onConnect(Consumer<SocketChannel> consumer);

	NetworkHubFactory workloadPerSelector(int workload);
}
