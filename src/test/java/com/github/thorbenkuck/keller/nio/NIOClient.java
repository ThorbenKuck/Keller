package com.github.thorbenkuck.keller.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class NIOClient {

	public static void main(String[] args) throws IOException, InterruptedException {

		InetSocketAddress crunchifyAddr = new InetSocketAddress("localhost", 1111);
		SocketChannel crunchifyClient = SocketChannel.open(crunchifyAddr);
		crunchifyClient.configureBlocking(false);
		Selector selector = Selector.open(); // selector is open here
		crunchifyClient.register(selector, SelectionKey.OP_READ);

		new Thread(() -> {
			while (true) {

				log("i'm a server and i'm waiting for new connection and buffer select...");
				// Selects a set of keys whose corresponding channels are ready for I/O operations
				try {
					selector.select();
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}

				// token representing the registration of a SelectableChannel with a Selector
				Set<SelectionKey> crunchifyKeys = selector.selectedKeys();
				Iterator<SelectionKey> crunchifyIterator = crunchifyKeys.iterator();

				while (crunchifyIterator.hasNext()) {
					SelectionKey myKey = crunchifyIterator.next();

					if (myKey.isReadable()) {

						SocketChannel server = (SocketChannel) myKey.channel();
						ByteBuffer crunchifyBuffer = ByteBuffer.allocate(256);
						try {
							server.read(crunchifyBuffer);
						} catch (IOException e) {
							e.printStackTrace();
							continue;
						}
						String result = new String(crunchifyBuffer.array()).trim();

						log("Message received: " + result);
					}
					crunchifyIterator.remove();
				}
			}
		}).start();

		log("Connecting to Server on port 1111...");

		ArrayList<String> companyDetails = new ArrayList<>();

		// create a ArrayList with companyName list
		companyDetails.add("Facebook");
		companyDetails.add("Twitter");
		companyDetails.add("IBM");
		companyDetails.add("Google");
		companyDetails.add("Crunchify");

		for (String companyName : companyDetails) {

			byte[] message = companyName.getBytes();
			ByteBuffer buffer = ByteBuffer.wrap(message);
			crunchifyClient.write(buffer);

			log("sending: " + companyName);
			buffer.clear();

			// wait for 2 seconds before sending next message
			Thread.sleep(2000);
		}
		crunchifyClient.close();
	}

	private static void log(String str) {
		System.out.println(str);
	}

}
