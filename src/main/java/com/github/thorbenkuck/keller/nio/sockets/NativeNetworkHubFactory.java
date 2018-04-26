package com.github.thorbenkuck.keller.nio.sockets;

import com.github.thorbenkuck.keller.utility.Keller;

import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

final class NativeNetworkHubFactory implements NetworkHubFactory {

	private NativeNetworkHub networkListener = new NativeNetworkHub();

	@Override
	public final NetworkHubFactory onObjectReceive(Consumer<Message> consumer) {
		Keller.parameterNotNull(consumer);
		networkListener.addReceivedListener(consumer);
		return this;
	}

	@Override
	public final NetworkHubFactory onConnect(Consumer<SocketChannel> consumer) {
		Keller.parameterNotNull(consumer);
		networkListener.addConnectedListener(consumer);
		return this;
	}

	@Override
	public final NetworkHubFactory onDisconnect(Consumer<SocketChannel> consumer) {
		Keller.parameterNotNull(consumer);
		networkListener.addDisconnectedListener(consumer);
		return this;
	}

	@Override
	public final NetworkHubFactory executorService(ExecutorService executorService) {
		Keller.parameterNotNull(executorService);
		networkListener.setExecutorService(executorService);
		return this;
	}

	@Override
	public final NetworkHubFactory onException(Consumer<Exception> ex) {
		Keller.parameterNotNull(ex);
		networkListener.setOnException(ex);
		return this;
	}

	@Override
	public final NetworkHubFactory bufferSize(int bufferSize) {
		networkListener.setBufferSize(bufferSize);
		return this;
	}

	@Override
	public final NetworkHubFactory workloadPerSelector(int workload) {
		networkListener.setMaxWorkloadPerSelector(workload);
		return this;
	}

	@Override
	public final NetworkHubFactory selectorChannelStrategy(ChoosingStrategy strategy) {
		networkListener.workloadDispenser().setChoosingStrategy(strategy);
		return this;
	}

	@Override
	public final NetworkHubFactory serializer(Function<Object, String> function) {
		Keller.parameterNotNull(function);
		networkListener.setSerializer(function);
		return this;
	}

	@Override
	public final NetworkHubFactory deserializer(Function<String, Object> function) {
		Keller.parameterNotNull(function);
		networkListener.setDeserializer(function);
		return this;
	}

	@Override
	public final NetworkHub build() {
		return networkListener;
	}
}
