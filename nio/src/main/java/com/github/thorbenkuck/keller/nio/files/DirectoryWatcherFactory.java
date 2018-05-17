package com.github.thorbenkuck.keller.nio.files;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public interface DirectoryWatcherFactory {

	static DirectoryWatcherFactory create() {
		return new NativeDirectoryWatcherFactory();
	}

	DirectoryWatcherFactory setExecutorService(ExecutorService executorService);

	DirectoryWatcherFactory ifNewFile(Consumer<Path> consumer);

	DirectoryWatcherFactory ifUpdatedFile(Consumer<Path> consumer);

	DirectoryWatcherFactory ifDeletedFile(Consumer<Path> consumer);

	DirectoryWatcherFactory addFileHandler(FileHandler handler);

	DirectoryWatcher get();

}
