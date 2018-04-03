package com.github.thorbenkuck.keller.nio.sockets;

import com.github.thorbenkuck.keller.utility.Keller;

import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

class NativeNetworkHubFactory implements NetworkHubFactory {

	private NativeNetworkHub networkListener = new NativeNetworkHub();

	@Override
	public NetworkHubFactory onObjectReceive(Consumer<Message> consumer) {
		Keller.parameterNotNull(consumer);
		networkListener.addReceivedListener(consumer);
		return this;
	}

	@Override
	public NetworkHubFactory onConnect(Consumer<SocketChannel> consumer) {
		Keller.parameterNotNull(consumer);
		networkListener.addConnectedListener(consumer);
		return this;
	}

	@Override
	public NetworkHubFactory onDisconnect(Consumer<SocketChannel> consumer) {
		Keller.parameterNotNull(consumer);
		networkListener.addDisconnectedListener(consumer);
		return this;
	}

	@Override
	public NetworkHubFactory executorService(ExecutorService executorService) {
		Keller.parameterNotNull(executorService);
		networkListener.setExecutorService(executorService);
		return this;
	}

	@Override
	public NetworkHubFactory onException(Consumer<Exception> ex) {
		Keller.parameterNotNull(ex);
		networkListener.setOnException(ex);
		return this;
	}

	@Override
	public NetworkHubFactory bufferSize(int bufferSize) {
		networkListener.setBufferSize(bufferSize);
		return this;
	}

	@Override
	public NetworkHubFactory workloadPerSelector(int workload) {
		networkListener.setMaxWorkloadPerSelector(workload);
		return this;
	}

	@Override
	public NetworkHubFactory serializer(Function<Object, String> function) {
		Keller.parameterNotNull(function);
		networkListener.setSerializer(function);
		return this;
	}

	@Override
	public NetworkHubFactory deserializer(Function<String, Object> function) {
		Keller.parameterNotNull(function);
		networkListener.setDeserializer(function);
		return this;
	}

	@Override
	public NetworkHub build() {
		return networkListener;
	}
}
