package com.github.thorbenkuck.keller.nioTest;

import com.github.thorbenkuck.keller.nio.files.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileWriterTest {

	private static final String TARGET = "/home/thorben/Test.txt";
	private static final FileReader fileReader = FileReader.create();
	private static final DirectoryWatcher watcher = DirectoryWatcherFactory.create()
			.ifUpdatedFile(FileWriterTest::updatedFile)
			.ifDeletedFile(FileWriterTest::deletedFile)
			.ifNewFile(FileWriterTest::newFile)
			.get();

	private static void handle(Exception e) {
		e.printStackTrace(System.out);
	}

	private static void handle(Path p) {
		try {
			fileReader.open(p);
			fileReader.read();
			fileReader.close();
		} catch (IOException e) {
			handle(e);
		} finally {
			fileReader.close();
		}
	}

	private static void newFile(Path path) {
		System.out.println("{1}: New File: " + path);
		handle(path);
	}

	private static void updatedFile(Path p) {
		System.out.println("{2}: Change in: " + p);
		handle(p);
	}

	private static void deletedFile(Path p) {
		System.out.println("{3}: Deleted: " + p);
	}

	private static void listenToChanges() throws DirectoryWatcherException {
		Path path = Paths.get(TARGET).toAbsolutePath().getParent();
		watcher.watch(path);
		System.out.println("Watching " + path);
	}

	public static void main(String[] args) {
		try {
			listenToChanges();
		} catch (DirectoryWatcherException e) {
			e.printStackTrace();
		}

		FileWriter fileWriter = FileWriter.create();
		fileReader.processLine(line -> line.prettyPrint(System.out));

		try {
			fileWriter.open(TARGET);
			fileWriter.append("I am just beneath the previous line.");
			fileWriter.newLine();
			fileWriter.close();
		} catch (IOException e) {
			handle(e);
			return;
		} finally {
			fileWriter.close();
		}

		try {
			System.out.println(Files.getLastModifiedTime(Paths.get(TARGET)));
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("\n\n-------\n\n");


		try {
			fileWriter.open(TARGET);
			fileWriter.clear();
			fileWriter.set("I am the king!");
			fileWriter.newLine();
			fileWriter.close();
		} catch (IOException e) {
			handle(e);
			return;
		} finally {
			fileWriter.close();
		}

		try {
			System.out.println(Files.getLastModifiedTime(Paths.get(TARGET)));
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		watcher.close();
	}

}
