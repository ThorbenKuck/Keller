package com.github.thorbenkuck.keller.nio.sockets;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ReceiveObjectListener implements Runnable {

	private final Selector selector;
	private final Supplier<ExecutorService> executorService;
	private final Supplier<Integer> bufferSize;
	private final ReceivedListener receivedListener;
	private final DisconnectedListener disconnectedListener;
	private final Deserializer deserializer;
	private final Consumer<Exception> onException;
	private final AtomicBoolean running = new AtomicBoolean(false);
	private final ReceivedBytesHandler receivedBytesHandler = new ReceivedBytesHandler();

	public ReceiveObjectListener(Selector selector, DisconnectedListener disconnectedListener, ReceivedListener receivedListener, Deserializer deserializer, Supplier<Integer> bufferSize, Supplier<ExecutorService> executorService, Consumer<Exception> onException) {
		this.selector = selector;
		this.executorService = executorService;
		this.bufferSize = bufferSize;
		this.receivedListener = receivedListener;
		this.disconnectedListener = disconnectedListener;
		this.deserializer = deserializer;
		this.onException = onException;
	}

	private void handle(Exception e) {
		onException.accept(e);
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
		System.out.println("Receive Listener connected");
		running.set(true);
		try {
			while (running.get()) {
				try {
					selector.select();
					if(selector.isOpen()) {
						handle(selector.selectedKeys());
					} else {
						stop();
					}
				} catch (IOException e) {
					handle(e);
					stop();
				}
			}
		} catch (Exception e) {
			handle(e);
		}
		// TODO change to callback? Or don't care? IDK...
		System.out.println("Receive Listener disconnected");
	}

	private void handle(Set<SelectionKey> selectionKeys) {
		Iterator<SelectionKey> iterator = selectionKeys.iterator();
		final Queue<Message> received = new LinkedList<>();
		while (iterator.hasNext()) {
			SelectionKey key = iterator.next();

			if (key.isValid() && key.isReadable()) {
				SocketChannel sender = (SocketChannel) key.channel();
				receivedBytesHandler.handle(sender, this::handleDisconnected, bufferSize, deserializer, received, onException);
			}

			iterator.remove();
		}

		trigger(received);
	}

	private void handleDisconnected(SocketChannel channel) {
		disconnectedListener.handle(channel);
		try {
			channel.close();
		} catch (IOException e) {
			handle(e);
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
