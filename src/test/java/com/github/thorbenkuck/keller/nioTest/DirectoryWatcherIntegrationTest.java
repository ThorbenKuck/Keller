package com.github.thorbenkuck.keller.nioTest;

import com.github.thorbenkuck.keller.nio.files.DirectoryWatcher;
import com.github.thorbenkuck.keller.nio.files.DirectoryWatcherException;
import com.github.thorbenkuck.keller.nio.files.DirectoryWatcherFactory;
import com.github.thorbenkuck.keller.nio.files.DirectoryWatcherHub;

import java.nio.file.Paths;

public class DirectoryWatcherIntegrationTest {

	public static void main(String[] args) {
		// Create a empty Directory watcher
		DirectoryWatcher watcher = DirectoryWatcherFactory.create()
				.get();
		// Shortcut for the first line
		DirectoryWatcher watcher1 = DirectoryWatcher.create();
		// Simple print out.
		DirectoryWatcher watcherWithConsumers = DirectoryWatcherFactory.create()
				.ifNewFile(path -> System.out.println("new file: " + path.getFileName()))
				.ifDeletedFile(path -> System.out.println("new file: " + path.getFileName()))
				.ifUpdatedFile(path -> System.out.println("new file: " + path.getFileName()))
				.get();

		try {
			// Create a hub, with the path ""
			DirectoryWatcherHub hub = DirectoryWatcherHub.build()
					.add(Paths.get(""))
					.initialize();
		} catch (DirectoryWatcherException e) {
			e.printStackTrace();
		}
		// Create a empty Hub
		DirectoryWatcherHub hub1 = DirectoryWatcherHub.build().empty();
		// Shortcut for the line above
		DirectoryWatcherHub hub2 = DirectoryWatcherHub.empty();
	}

}
