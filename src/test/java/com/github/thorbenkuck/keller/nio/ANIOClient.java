package com.github.thorbenkuck.keller.nio;

import com.github.thorbenkuck.keller.nio.sockets.ReactiveNIONetworkWorker;

import java.io.IOException;
import java.util.ArrayList;

public class ANIOClient {

	public static void main(String[] args) {
		ReactiveNIONetworkWorker worker = new ReactiveNIONetworkWorker();
		worker.setSerializer(new JavaSerializer());
		worker.setDeSerializer(new JavaDeserializer());

		worker.addReceivedListener(message -> System.out.println(message.getContent()));

		try {
			worker.initialize("localhost", 4444);
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
				worker.send(new TestObject(companyName));
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
			worker.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void log(String s) {
		System.out.println(s);
	}

}
