package com.github.thorbenkuck.keller.nio;

import com.github.thorbenkuck.keller.nio.sockets.NetworkHub;
import com.github.thorbenkuck.keller.nio.sockets.NetworkHubFactory;

import java.io.IOException;

public class ANIOServer {

	public static void main(String[] args) {
		NetworkHub hub = NetworkHubFactory.create()
				.serializer(new JavaSerializer())
				.deserializer(new JavaDeserializer())
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
					System.out.println("Sending: " + toSendBack);
					hub.send(toSendBack, message.getChannel());
				} catch (IOException e) {
					e.printStackTrace();
				}
			});

			hub.addReceivedListener(message -> System.out.println(message.getContent()));

			hub.addDisconnectedListener(System.out::println);

			hub.initialize(4444);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
