package com.github.thorbenkuck.keller.nio.sockets;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

class Worker implements Runnable {

	private final Selector selector;
	private final DisconnectedListener disconnectedListener;
	private final ReceivedListener receivedListener;
	private final Supplier<Integer> byteBuffer;
	private final AtomicBoolean running = new AtomicBoolean(false);
	private final Supplier<ExecutorService> executorService;
	private final Deserializer deserializer;
	private final ReceivedBytesHandler receivedBytesHandler = new ReceivedBytesHandler();

	Worker(Selector selector, DisconnectedListener disconnectedListener, ReceivedListener receivedListener, Deserializer deserializer, Supplier<Integer> byteBuffer, Supplier<ExecutorService> executorService) {
		this.selector = selector;
		this.disconnectedListener = disconnectedListener;
		this.receivedListener = receivedListener;
		this.byteBuffer = byteBuffer;
		this.executorService = executorService;
		this.deserializer = deserializer;
	}

	/**
	 * When an object implementing interface <code>Runnable</code> is used
	 * to create a thread, starting the thread causes the object's
	 * <code>run</code> method to be called in that separately executing
	 * thread.
	 * <p>
	 * The general contract of the method <code>run</code> is that it may
	 * take any action whatsoever.
	 *
	 * @see Thread#run()
	 */
	@Override
	public void run() {
		running.set(true);
		while (running.get()) {
			try {
				selector.select();
				handle(selector.selectedKeys());
			} catch (IOException e) {
				e.printStackTrace(System.out);
				stop();
			}
		}
	}

	private void handle(Set<SelectionKey> keys) {
		Iterator<SelectionKey> iterator = keys.iterator();
		final Queue<Message> received = new LinkedList<>();
		while (iterator.hasNext()) {
			SelectionKey key = iterator.next();
			if (!key.isValid()) {
				SocketChannel channel = (SocketChannel) key.channel();
				handleDisconnected(channel);
				break;
			}

			if (key.isReadable()) {
				SocketChannel sender = (SocketChannel) key.channel();
				receivedBytesHandler.handle(sender, this::handleDisconnected, byteBuffer, deserializer, received);
			}
			iterator.remove();
		}

		trigger(received);
	}

	private void handleDisconnected(SocketChannel hub) {
		disconnectedListener.handle(hub);
		try {
			hub.close();
			hub.keyFor(selector).cancel();
			stop();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
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

	public void stop() {
		running.set(false);
	}
}
