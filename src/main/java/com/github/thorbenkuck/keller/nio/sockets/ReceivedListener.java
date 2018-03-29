package com.github.thorbenkuck.keller.nio.sockets;

import com.github.thorbenkuck.keller.pipe.Pipeline;

import java.util.function.Consumer;

class ReceivedListener {

	private final Pipeline<Message> messagePipeline = Pipeline.unifiedCreation();

	public void handle(Message message) {
		try {
			messagePipeline.apply(message);
		} catch (Throwable t) {
			t.printStackTrace(System.out);
		}
	}

	public void add(Consumer<Message> consumer) {
		messagePipeline.addFirst(consumer);
	}
}
