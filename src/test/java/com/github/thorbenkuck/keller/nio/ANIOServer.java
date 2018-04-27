package com.github.thorbenkuck.keller.nio;

import com.github.thorbenkuck.keller.nio.sockets.*;

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

	private static void addException(Throwable e) {
		synchronized (encountered) {
			encountered.add(e);
		}
	}

	private static void printExceptions() {
		System.out.println("\n\n\n");
		if(encountered.isEmpty()) {
			System.out.println("No Exceptions encountered");
		} else {
			System.out.println("-----------Encountered Exceptions-----------");
		}
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
//		System.out.println("################################### Received: " + message.getContent());
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

//		try {
//			System.out.println("Disconnected " + channel.getRemoteAddress() + "(left over: " + workloadDispenser.countConnectNodes() + "in " + workloadDispenser.countSelectorChannels() + " SelectorChannels" + ")");
//		} catch (IOException e) {
//			addException(e);
//		}
//		synchronized (receivedCounter) {
//			if(receivedCounter.get(channel) == null) {
//				count = 0;
//			} else {
//				count = receivedCounter.remove(channel);
//			}
//		}
//		if(count < 4) {
//			System.err.println("Missing some Objects! Expected: " + 4 + " received: " +count);
//			synchronized (failed) {
//				failed.incrementAndGet();
//			}
//		}
	}

	private static void printFails() {
		PrintStream out;
		if(failed.get() == 0) {
			out = System.out;
		} else {
			out = System.err;
		}
		out.println("\nFailed " + failed.get() + " times!\n");

		printExceptions();
	}

	public static void main(String[] args) {
		System.out.println("Server initialized with " + Runtime.getRuntime().availableProcessors() + " available cores.");
		System.out.println("Server initialized with " + Runtime.getRuntime().maxMemory() + " available memory.");
		Runtime.getRuntime().addShutdownHook(new Thread(ANIOServer::printFails));
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> addException(e));
		ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);

		NetworkHub hub = NetworkHubFactory.create()
				.serializer(new JavaSerializer())
				.deserializer(new JavaDeserializer())
				.onObjectReceive(ANIOServer::received)
				.onDisconnect(ANIOServer::disconnected)
				.onException(ANIOServer::addException)
				.selectorChannelStrategy(ChoosingStrategy.lowestWorkloadFirst())
				.unlimitedWorkloadPerSelector()
				.bufferSize(1024)
				.build();

		try {
			hub.addConnectedListener(socketChannel -> {
//				try {
//					System.out.println("Connected: " + socketChannel.getLocalAddress());
//				} catch (IOException e) {
//					addException(e);
//				}
			});

			hub.addReceivedListener(message -> {
				try {
					hub.send(new TestObject("ACK"), message.getChannel());
				} catch (IOException e) {
					addException(e);
				}
			});

			hub.open(4445);
			System.out.println("Server connected to port " + 4445);

//			try {
//				Thread.sleep(10000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//
//			System.out.println("Closing NetworkHub");
//			hub.clear();
//			System.out.println("Closed");
		} catch (IOException e) {
			addException(e);
			System.err.println("Server terminated!");
			System.exit(1);
		}
		scheduledExecutorService.scheduleAtFixedRate(() -> collect(hub.workloadDispenser()), 1, 1, TimeUnit.SECONDS);
	}

	private static void collect(WorkloadDispenser workloadDispenser) {
		final List<ReadOnlySelectorChannelInformation> information = workloadDispenser.dumpInformation();
		if(information.isEmpty()) {
			return;
		}
//		System.out.println("COLLECTING_CORPSES .. (" + workloadDispenser.countConnectNodes() + " IN " + workloadDispenser.countSelectorChannels() + ")");
		workloadDispenser.collectCorpses();
//		System.out.println("COLLECTING_CORPSES finished");

		System.out.println("\nFound " + information.size() + " SelectorChannels");
		int totalSockets = 0;
		double totalWorkload = 0.0;
		for(ReadOnlySelectorChannelInformation inf : information) {
			totalSockets += inf.getStoredSocketChannels();
			totalSockets += inf.workloadPercentage();
			System.out.println(inf);
		}
		System.out.println(totalSockets + " SocketChannels found. Total workload: " + totalWorkload );
		System.out.println();
	}
}
