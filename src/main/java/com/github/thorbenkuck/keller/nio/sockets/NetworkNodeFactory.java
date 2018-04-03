package com.github.thorbenkuck.keller.nio.sockets;

public interface NetworkNodeFactory extends NetworkFactory<NetworkNode, NetworkNodeFactory> {

	static NetworkNodeFactory create() {
		return new NativeNetworkNodeFactory();
	}
}