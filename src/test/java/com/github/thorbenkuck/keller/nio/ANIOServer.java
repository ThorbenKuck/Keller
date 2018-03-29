package com.github.thorbenkuck.keller.nio;

import com.github.thorbenkuck.keller.nio.sockets.NetworkListener;
import com.github.thorbenkuck.keller.nio.sockets.NetworkListenerFactory;

import java.io.IOException;

public class ANIOServer {

	public static void main(String[] args) {
		NetworkListener listener = NetworkListenerFactory.create()
				.serializer(new JavaSerializer())
				.deserializer(new JavaDeserializer())
				.build();

		try {
			listener.addConnectedListener(socketChannel -> {
				try {
					System.out.println("Connected: " + socketChannel.getLocalAddress());
				} catch (IOException e) {
					e.printStackTrace();
				}
			});

			listener.addReceivedListener(message -> {
				try {
					Object toSendBack = message.getContent();
					System.out.println("Sending: " + toSendBack);
					listener.send(toSendBack, message.getChannel());
				} catch (IOException e) {
					e.printStackTrace();
				}
			});

			listener.addReceivedListener(message -> System.out.println(message.getContent()));

			listener.addDisconnectedListener(System.out::println);

			listener.initialize(4444);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
