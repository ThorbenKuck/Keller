package com.github.thorbenkuck.keller.nio;

import com.github.thorbenkuck.keller.nio.sockets.Deserializer;
import com.github.thorbenkuck.keller.nio.sockets.ReactiveNIONetworkListener;
import com.github.thorbenkuck.keller.nio.sockets.Sender;

import java.io.IOException;

public class ANIOServer {

	public static void main(String[] args) {

		Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
			exception.printStackTrace(System.out);
		});

		ReactiveNIONetworkListener listener = new ReactiveNIONetworkListener();
		Sender sender = new Sender();
		Deserializer deserializer = new Deserializer();
		deserializer.setDeserializer(new JavaDeserializer());
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
					System.out.println("Received");
					sender.send(deserializer.getDeSerializedContent(message), message.getChannel());
				} catch (IOException e) {
					e.printStackTrace();
				}
			});

			listener.addReceivedListener(message -> {
				Object content = deserializer.getDeSerializedContent(message);
				System.out.println(content);
			});

			listener.initialize(4444);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
