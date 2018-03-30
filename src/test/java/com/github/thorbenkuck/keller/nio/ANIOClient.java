package com.github.thorbenkuck.keller.nio;

import com.github.thorbenkuck.keller.nio.sockets.NetworkNode;
import com.github.thorbenkuck.keller.nio.sockets.NetworkNodeFactory;

import java.io.IOException;
import java.util.ArrayList;

public class ANIOClient {

	public static void main(String[] args) {
		NetworkNode node = NetworkNodeFactory.create()
				.serializer(new JavaSerializer())
				.deserializer(new JavaDeserializer())
				.ifObjectArrives(message -> System.out.println(message.getContent()))
				.build();

		try {
			node.initialize("localhost", 4444);
		} catch (IOException e) {
			e.printStackTrace();
		}

		ArrayList<String> companyDetails = new ArrayList<>();

		// create a ArrayList with companyName list
		companyDetails.add("Facebook");
		companyDetails.add("Twitter");
		companyDetails.add("IBM");
		companyDetails.add("Google");
		companyDetails.add("Crunchify");

		for (String companyName : companyDetails) {

			log("Sending " + companyName + " .. ");
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

		System.out.println("Closing ..");
		try {
			node.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void log(String s) {
		System.out.println(s);
	}

}
