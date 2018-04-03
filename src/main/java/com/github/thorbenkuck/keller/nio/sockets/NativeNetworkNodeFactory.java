package com.github.thorbenkuck.keller.nio.sockets;

import com.github.thorbenkuck.keller.annotations.Experimental;
import com.github.thorbenkuck.keller.utility.Keller;

import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

class NativeNetworkNodeFactory implements NetworkNodeFactory {

	private NativeNetworkNode networkWorker = new NativeNetworkNode();

	@Override
	public NetworkNodeFactory onObjectReceive(Consumer<Message> consumer) {
		Keller.parameterNotNull(consumer);
		networkWorker.addReceivedListener(consumer);
		return this;
	}

	@Experimental
	@Override
	public NetworkNodeFactory onDisconnect(Consumer<SocketChannel> consumer) {
		Keller.parameterNotNull(consumer);
		networkWorker.addDisconnectedListener(consumer);
		return this;
	}

	@Override
	public NetworkNodeFactory executorService(ExecutorService executorService) {
		Keller.parameterNotNull(executorService);
		networkWorker.setExecutorService(executorService);
		return this;
	}

	@Override
	public NetworkNodeFactory onException(Consumer<Exception> ex) {
		Keller.parameterNotNull(ex);
		networkWorker.setExceptionConsumer(ex);
		return this;
	}

	@Override
	public NetworkNodeFactory bufferSize(int bufferSize) {
		Keller.parameterNotNull(bufferSize);
		networkWorker.setBufferSize(bufferSize);

		return this;
	}

	@Override
	public NetworkNodeFactory serializer(Function<Object, String> function) {
		Keller.parameterNotNull(function);
		networkWorker.setSerializer(function);

		return this;
	}

	@Override
	public NetworkNodeFactory deserializer(Function<String, Object> function) {
		Keller.parameterNotNull(function);
		networkWorker.setDeSerializer(function);

		return this;
	}

	@Override
	public NetworkNode build() {
		return networkWorker;
	}
}
