package com.github.thorbenkuck.keller.nio.sockets;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Supplier;

public class Worker implements Runnable {

	private final Selector selector;
	private final ReceivedListener receivedListener;
	private final Supplier<Integer> byteBuffer;

	public Worker(Selector selector, ReceivedListener receivedListener, Supplier<Integer> byteBuffer) {
		this.selector = selector;
		this.receivedListener = receivedListener;
		this.byteBuffer = byteBuffer;
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
				// TODO: Disconnected here, handle in listener
				continue;
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
}
