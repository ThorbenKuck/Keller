package com.github.thorbenkuck.keller.nio;

import com.github.thorbenkuck.keller.nio.sockets.NetworkHub;
import com.github.thorbenkuck.keller.nio.sockets.NetworkHubFactory;

import java.io.IOException;

public class ANIOServer {

	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> e.printStackTrace(System.out));

		NetworkHub hub = NetworkHubFactory.create()
				.serializer(new JavaSerializer())
				.deserializer(new JavaDeserializer())
				.workloadPerSelector(1)
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

			hub.addDisconnectedListener(channel -> {
				try {
					System.out.println("Disconnected " + channel.getLocalAddress());
				} catch (IOException e) {
					e.printStackTrace();
				}
			});

			hub.open(4444);
			System.out.println("Server connected to port " + 4444);

//			try {
//				Thread.sleep(40000);
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
	}

}
