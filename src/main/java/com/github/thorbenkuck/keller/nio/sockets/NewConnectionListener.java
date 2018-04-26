package com.github.thorbenkuck.keller.nio.sockets;

import java.io.IOException;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

final class NewConnectionListener implements Runnable {

	private final Selector selector;
	private final ConnectedListener connectedListener;
	private final ServerSocketChannel serverSocketChannel;
	private final AtomicBoolean running = new AtomicBoolean(false);

	NewConnectionListener(Selector selector, ConnectedListener connectedListener, ServerSocketChannel serverSocketChannel) {
		this.selector = selector;
		this.connectedListener = connectedListener;
		this.serverSocketChannel = serverSocketChannel;
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
	public final void run() {
		running.set(true);
		try {
			while (running.get()) {
				try {
					selector.select();
					handle(selector.selectedKeys());
				} catch (IOException e) {
					running.set(false);
					e.printStackTrace(System.out);
				}
			}
		} catch (Throwable t) {
			t.printStackTrace(System.out);
			running.set(false);
		}
	}

	private void handle(Set<SelectionKey> selectionKeys) {
		Iterator<SelectionKey> iterator = selectionKeys.iterator();
		while (iterator.hasNext()) {
			SelectionKey key = iterator.next();

			if (key.isAcceptable()) {
				try {
					final SocketChannel connected = serverSocketChannel.accept();
					connected.configureBlocking(false);
					connectedListener.handle(connected);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			iterator.remove();
		}
	}

	public final void stop() {
		running.set(false);
	}
}
