package com.github.thorbenkuck.keller.nio;

import com.github.thorbenkuck.keller.nio.sockets.Message;
import com.github.thorbenkuck.keller.nio.sockets.NetworkHub;
import com.github.thorbenkuck.keller.nio.sockets.NetworkHubFactory;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ANIOServer {

	private static final Map<SocketChannel, Integer> receivedCounter = new HashMap<>();
	private static final AtomicInteger failed = new AtomicInteger(0);

	private static void received(Message message) {
		System.out.println(message.getContent());
		synchronized (receivedCounter) {
			int current = receivedCounter.getOrDefault(message.getChannel(), 0);
			receivedCounter.put(message.getChannel(), ++current);
		}
	}

	private static void disconnected(SocketChannel channel) {
		int count;
		synchronized (receivedCounter) {
			count = receivedCounter.remove(channel);
		}
		if(count < 4) {
			System.err.println("Missing some Objects! Expected: " + 4 + " received: " +count);
			synchronized (failed) {
				failed.incrementAndGet();
			}
		}
	}

	private static void printFails() {
		PrintStream out;
		if(failed.get() == 0) {
			out = System.out;
		} else {
			out = System.err;
		}
		out.println("Failed " + failed.get() + " times!");
	}

	public static void main(String[] args) {
		Runtime.getRuntime().addShutdownHook(new Thread(ANIOServer::printFails));
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> e.printStackTrace(System.out));
		ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

		NetworkHub hub = NetworkHubFactory.create()
				.serializer(new JavaSerializer())
				.deserializer(new JavaDeserializer())
				.onObjectReceive(ANIOServer::received)
				.onDisconnect(ANIOServer::disconnected)
				.bufferSize(1024)
				.workloadPerSelector(1000)
				.build();

		try {
			hub.addConnectedListener(socketChannel -> {
				try {
					System.out.println("Connected: " + socketChannel.getLocalAddress());
				} catch (IOException e) {
					e.printStackTrace();
				}
			});

			hub.addReceivedListener(message -> {
				try {
					Object toSendBack = message.getContent();
					hub.send(toSendBack, message.getChannel());
				} catch (IOException e) {
					e.printStackTrace();
				}
			});

			hub.open(4444);
			System.out.println("Server connected to port " + 4444);

//			try {
//				Thread.sleep(10000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//
//			System.out.println("Closing NetworkHub");
//			hub.close();
//			System.out.println("Closed");
		} catch (IOException e) {
			e.printStackTrace();
		}
		scheduledExecutorService.scheduleAtFixedRate(hub.workloadDispenser()::collectCorpses, 10, 10, TimeUnit.SECONDS);
	}

}
