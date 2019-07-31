package com.github.thorbenkuck.keller.reactive;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class EventStreamTest {

	private static final PrintStream printStream = new PrintStream(new ByteArrayOutputStream());

	public static void main(String[] args) {
		WritableEventStream<String> eventStream = new SimpleEventStream<>();
		for(int i = 0 ; i < 10000 ; i++) {
			eventStream.subscribe(printStream::println);
		}

		for(int i = 0 ; i < 100 ; i++) {
			long start = System.currentTimeMillis();
			eventStream.push("Hello");
			long end = System.currentTimeMillis();
			System.out.println(end - start);
		}
	}

}
