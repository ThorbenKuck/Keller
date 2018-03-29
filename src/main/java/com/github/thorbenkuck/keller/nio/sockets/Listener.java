package com.github.thorbenkuck.keller.nio.sockets;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

public class Listener implements Runnable {

	private final Selector selector;
	private final Supplier<ExecutorService> executorService;
	private final ConnectedListener connectedListener;
	private final Supplier<Integer> bufferSize;
	private final ReceivedListener listener;
	private final ServerSocketChannel serverSocketChannel;

	public Listener(Selector selector, ServerSocketChannel serverSocketChannel, ReceivedListener listener, ConnectedListener connectedListener,
	                Supplier<ExecutorService> executorService, Supplier<Integer> bufferSize) {
		this.selector = selector;
		this.executorService = executorService;
		this.serverSocketChannel = serverSocketChannel;
		this.connectedListener = connectedListener;
		this.bufferSize = bufferSize;
		this.listener = listener;
	}

	public void run() {
		try {
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, null);
		} catch (ClosedChannelException e) {
			e.printStackTrace();
		}
		while (true) {
			try {
				System.out.println("Awaiting Selector ...");
				selector.select();
				handle(selector.selectedKeys());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void handle(Set<SelectionKey> keys) {
		Iterator<SelectionKey> iterator = keys.iterator();
		while (iterator.hasNext()) {
			SelectionKey key = iterator.next();
			if (!key.isValid()) {
				System.out.println("Found invalid key..");
				// TODO: Disconnected here, handle in listener
				continue;
			}

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
				System.out.println("Message received: ");
				ByteBuffer buffer = ByteBuffer.allocate(bufferSize.get());
				try {
					sender.read(buffer);
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
				String result = new String(buffer.array()).trim();
				listener.handle(new Message(result, sender));
			}
			iterator.remove();
		}
	}
}
