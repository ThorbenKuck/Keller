package com.github.thorbenkuck.keller.nio.sockets;

import java.nio.channels.SocketChannel;
import java.util.function.Consumer;

public interface NetworkHubFactory extends NetworkFactory<NetworkHub, NetworkHubFactory> {

	static NetworkHubFactory create() {
		return new NativeNetworkHubFactory();
	}

	NetworkHubFactory onConnect(final Consumer<SocketChannel> consumer);

	NetworkHubFactory workloadPerSelector(final int workload);

	default NetworkHubFactory unlimitedWorkloadPerSelector() {
		return workloadPerSelector(-1);
	}

	NetworkHubFactory selectorChannelStrategy(ChoosingStrategy strategy);
}
