package com.github.thorbenkuck.keller.nio;

import com.github.thorbenkuck.keller.nio.sockets.NetworkNode;
import com.github.thorbenkuck.keller.nio.sockets.NetworkNodeFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

public class ANIOClient {

	public ANIOClient(int count) {
		NetworkNode node = NetworkNodeFactory.create()
				.serializer(new JavaSerializer())
				.deserializer(new JavaDeserializer())
				.ifObjectArrives(System.out::println)
				.onDisconnect(channel -> decreaseCount())
				.build();

		try {
			node.open("localhost", 4444);
		} catch (IOException e) {
			e.printStackTrace();
			decreaseCount();
			return;
		}

		System.out.println(count + " connected!");

		ArrayList<String> companyDetails = new ArrayList<>();

		// create a ArrayList with companyName list
		companyDetails.add("Facebook");
		companyDetails.add("Twitter");
		companyDetails.add("IBM");
		companyDetails.add("Google");

		for (String companyName : companyDetails) {

			try {
				node.send(new TestObject(companyName));
			} catch (IOException e) {
				e.printStackTrace();
			}

			// wait for 2 seconds before sending next message
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		try {
			node.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(count + " closed: " + !node.isOpen());
		decreaseCount();
	}

	static int count = 0;

	public static void decreaseCount() {
		synchronized (ANIOClient.class) {
			--count;
		}
	}

	public static void increaseCount() {
		synchronized (ANIOClient.class) {
			++count;
		}
	}

	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> e.printStackTrace(System.out));
		ExecutorService executorService = Executors.newCachedThreadPool();

		while(true) {
			increaseCount();
			int finalCount = count;
			executorService.submit(() -> new ANIOClient(finalCount));
			try {
				Thread.sleep(ThreadLocalRandom.current().nextInt(1, 10));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
