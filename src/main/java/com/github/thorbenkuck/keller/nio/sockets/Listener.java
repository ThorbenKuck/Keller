package com.github.thorbenkuck.keller.nio.sockets;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

class Listener implements Runnable {

	private final Selector selector;
	private final Supplier<ExecutorService> executorService;
	private final ConnectedListener connectedListener;
	private final Supplier<Integer> bufferSize;
	private final ReceivedListener receivedListener;
	private final ServerSocketChannel serverSocketChannel;
	private final DisconnectedListener disconnectedListener;
	private final Deserializer deserializer;
	private final AtomicBoolean running = new AtomicBoolean(false);

	Listener(Selector selector, ServerSocketChannel serverSocketChannel, ReceivedListener receivedListener, ConnectedListener connectedListener,
	         DisconnectedListener disconnectedListener, Supplier<ExecutorService> executorService, Supplier<Integer> bufferSize, Deserializer deserializer) {
		this.selector = selector;
		this.disconnectedListener = disconnectedListener;
		this.executorService = executorService;
		this.serverSocketChannel = serverSocketChannel;
		this.connectedListener = connectedListener;
		this.bufferSize = bufferSize;
		this.receivedListener = receivedListener;
		this.deserializer = deserializer;
	}

	public void run() {
		try {
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, null);
		} catch (ClosedChannelException e) {
			e.printStackTrace();
			return;
		}
		running.set(true);
		while (running.get()) {
			try {
				selector.select();
				handle(selector.selectedKeys());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void handle(Set<SelectionKey> keys) {
		Iterator<SelectionKey> iterator = keys.iterator();
		final Queue<Message> received = new LinkedList<>();
		while (iterator.hasNext()) {
			SelectionKey key = iterator.next();

			if (key.isAcceptable()) {
				System.out.println("New Connection detected ..");
				try {
					final SocketChannel connected = serverSocketChannel.accept();
					connected.configureBlocking(false);
					connected.register(selector, SelectionKey.OP_READ);
					connectedListener.handle(connected);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (key.isReadable()) {
				SocketChannel sender = (SocketChannel) key.channel();
				if(!sender.isConnected()) {
					SocketChannel channel = (SocketChannel) key.channel();
					disconnectedListener.handle(channel);
					continue;
				}
				ByteBuffer buffer = ByteBuffer.allocate(bufferSize.get());
				try {
					sender.read(buffer);
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
				String result = new String(buffer.array()).trim();
				received.add(new Message(deserializer.getDeSerializedContent(result), sender));
			}
			iterator.remove();
		}

		trigger(received);
	}

	private void trigger(final Queue<Message> objects) {
		if(objects.isEmpty()) {
			return;
		}
		final Queue<Message> toWorkOn = new LinkedList<>(objects);
		executorService.get().submit(() -> {
			while(toWorkOn.peek() != null) {
				Message message = toWorkOn.poll();
				receivedListener.handle(message);
			}
		});
	}
}
