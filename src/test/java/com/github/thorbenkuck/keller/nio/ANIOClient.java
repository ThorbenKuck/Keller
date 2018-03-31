package com.github.thorbenkuck.keller.nio;

import com.github.thorbenkuck.keller.nio.sockets.NetworkNode;
import com.github.thorbenkuck.keller.nio.sockets.NetworkNodeFactory;
import javafx.application.Platform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

public class ANIOClient {

	private final int myCount;

	private void disconnect() {
		System.out.println(myCount + " disconnected ..");
		decreaseCount();
	}

	public ANIOClient(int count) {
		this.myCount = count;
		NetworkNode node = NetworkNodeFactory.create()
				.serializer(new JavaSerializer())
				.deserializer(new JavaDeserializer())
				.onObjectReceive(System.out::println)
				.onDisconnect(channel -> disconnect())
				.onException(ANIOClient::addException)
				.build();

		try {
			node.open("localhost", 4444);
		} catch (IOException e) {
			System.out.println(myCount + " could not connect!");
			decreaseCount();
			return;
		}

		System.out.println("Connected: " + myCount);

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
				disconnect();
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
		System.out.println("Disconnected " + myCount);
		decreaseCount();
	}

	private static int count = 0;
	private static final List<Throwable> encountered = new ArrayList<>();

	private static void addException(Throwable e) {
		synchronized (encountered) {
			encountered.add(e);
		}
	}

	private static void printExceptions() {
		synchronized (encountered) {
			for(Throwable exception : encountered) {
				exception.printStackTrace(System.out);
				System.out.println("\n\n");
			}
		}
	}

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
		Runtime.getRuntime().addShutdownHook(new Thread(ANIOClient::printExceptions));
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> addException(e));
		ExecutorService executorService = Executors.newCachedThreadPool();
		final ThreadLocalRandom random = ThreadLocalRandom.current();

		while(true) {
			increaseCount();
			int finalCount = count;
			executorService.submit(() -> new ANIOClient(finalCount));
			try {
				Thread.sleep(random.nextInt(10, 100));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
