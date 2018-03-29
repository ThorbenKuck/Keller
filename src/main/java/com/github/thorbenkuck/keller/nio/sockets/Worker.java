package com.github.thorbenkuck.keller.nio.sockets;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class Worker implements Runnable {

	private final Selector selector;
	private final DisconnectedListener disconnectedListener;
	private final ReceivedListener receivedListener;
	private final Supplier<Integer> byteBuffer;
	private final AtomicBoolean running = new AtomicBoolean(false);
	private final Supplier<ExecutorService> executorService;

	public Worker(Selector selector, DisconnectedListener disconnectedListener, ReceivedListener receivedListener, Supplier<Integer> byteBuffer, Supplier<ExecutorService> executorService) {
		this.selector = selector;
		this.disconnectedListener = disconnectedListener;
		this.receivedListener = receivedListener;
		this.byteBuffer = byteBuffer;
		this.executorService = executorService;
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
		while (true) {
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
		while (iterator.hasNext()) {
			SelectionKey key = iterator.next();
			if (!key.isValid()) {
				SocketChannel channel = (SocketChannel) key.channel();
				running.set(false);
				disconnectedListener.handle(channel);
				break;
			}

			if (key.isReadable()) {
				SocketChannel sender = (SocketChannel) key.channel();
				ByteBuffer buffer = ByteBuffer.allocate(byteBuffer.get());
				try {
					sender.read(buffer);
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
				String result = new String(buffer.array()).trim();
				receivedListener.handle(new Message(result, sender));
			}
			iterator.remove();
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
}
