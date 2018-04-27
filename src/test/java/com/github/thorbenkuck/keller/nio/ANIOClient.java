package com.github.thorbenkuck.keller.nio;

import com.github.thorbenkuck.keller.nio.sockets.Message;
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
import java.util.concurrent.TimeUnit;

public class ANIOClient {

	private final int myCount;

	private void disconnect() {
		System.out.println(myCount + " disconnected ..");
		decreaseCount();
	}

	private void handle(Message message) {
		System.out.println("########################################" + message.getContent());
		if(message.getContent().getClass().equals(TestObject.class)) {
			TestObject content = (TestObject) message.getContent();
			if(content.getString().equals("ACK")) {
				ack();
			}
		}
	}

	public ANIOClient(int count) {
		this.myCount = count;
		NetworkNode node = NetworkNodeFactory.create()
				.serializer(new JavaSerializer())
				.deserializer(new JavaDeserializer())
				.onObjectReceive(this::handle)
				.onDisconnect(channel -> disconnect())
				.onException(ANIOClient::addException)
				.build();

		try {
			node.open("localhost", 4445);
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
				send();
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
	private static int balance = 0;

	public static void send() {
		++balance;
	}

	public static void ack() {
		--balance;
	}

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

		System.out.println("Missing " + balance + " ACKs");
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

		long start = System.currentTimeMillis();
		boolean running = true;
		while(running) {
			if(TimeUnit.MILLISECONDS.toSeconds(start - System.currentTimeMillis()) == 2) {
				running = false;
			}
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
