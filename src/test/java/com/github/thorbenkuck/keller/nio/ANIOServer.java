package com.github.thorbenkuck.keller.nio;

import com.github.thorbenkuck.keller.nio.sockets.Message;
import com.github.thorbenkuck.keller.nio.sockets.NetworkHub;
import com.github.thorbenkuck.keller.nio.sockets.NetworkHubFactory;
import com.github.thorbenkuck.keller.nio.sockets.WorkloadDispenser;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ANIOServer {

	private static final Map<SocketChannel, Integer> receivedCounter = new HashMap<>();
	private static final AtomicInteger failed = new AtomicInteger(0);
	private static final List<Throwable> encountered = new ArrayList<>();
	private static WorkloadDispenser workloadDispenser;

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

	private static void received(Message message) {
		if(message == null) {
			System.out.println("... received null message ...");
			return;
		}
		System.out.println("Received: " + message.getContent());
		synchronized (receivedCounter) {
			int current = receivedCounter.getOrDefault(message.getChannel(), 0);
			receivedCounter.put(message.getChannel(), ++current);
		}
	}

	private static void disconnected(SocketChannel channel) {
		int count;
		if(channel == null) {
			return;
		}
		try {
			System.out.println("Disconnected " + channel.getRemoteAddress() + "(left over: " + workloadDispenser.countConnectNodes() + ")");
		} catch (IOException e) {
			e.printStackTrace();
		}
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

		printExceptions();
	}

	public static void main(String[] args) {
		Runtime.getRuntime().addShutdownHook(new Thread(ANIOServer::printFails));
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> addException(e));
		ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);

		NetworkHub hub = NetworkHubFactory.create()
				.serializer(new JavaSerializer())
				.deserializer(new JavaDeserializer())
				.onObjectReceive(ANIOServer::received)
				.onDisconnect(ANIOServer::disconnected)
				.onException(ANIOServer::addException)
				.bufferSize(1024)
				.build();

		workloadDispenser = hub.workloadDispenser();

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
					hub.send(new TestObject("ACK"), message.getChannel());
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
		scheduledExecutorService.scheduleAtFixedRate(() -> collect(hub.workloadDispenser()), 10, 10, TimeUnit.SECONDS);
		scheduledExecutorService.scheduleAtFixedRate(() -> deepCollect(hub.workloadDispenser()), 30, 30, TimeUnit.SECONDS);
	}

	private static void collect(WorkloadDispenser workloadDispenser) {
		System.out.println("COLLECTING_CORPSES .. (" + workloadDispenser.countConnectNodes() + " IN " + workloadDispenser.countSelectorChannels() + ")");
		workloadDispenser.collectCorpses();
		System.out.println("COLLECTING_CORPSES finished");
	}

	private static void deepCollect(WorkloadDispenser dispenser) {
		System.out.println("DEEPLY_COLLECTING_CORPSES .. (" + workloadDispenser.countConnectNodes() + " IN " + workloadDispenser.countSelectorChannels() + ")");
		workloadDispenser.collectCorpses();
		System.out.println("DEEPLY_COLLECTING_CORPSES finished");
	}

}
