package com.github.thorbenkuck.keller.nio.files;

import java.nio.file.Path;
import java.util.function.Consumer;

public interface DirectoryWatcher {

	static DirectoryWatcher create() {
		return DirectoryWatcherFactory.create().get();
	}

	void onNewFile(Consumer<Path> consumer);

	void onModifiedFile(Consumer<Path> consumer);

	void onDeletedFile(Consumer<Path> consumer);

	void addFileHandler(FileHandler fileHandler);

	void watch(String path) throws DirectoryWatcherException;

	void watch(Path directoryPath) throws DirectoryWatcherException;

	void stopWatching();
}
