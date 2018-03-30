package com.github.thorbenkuck.keller.nio.files;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

class NativeDirectoryWatcherFactory implements DirectoryWatcherFactory {

	private final NativeDirectoryWatcher watcher = new NativeDirectoryWatcher();

	@Override
	public DirectoryWatcherFactory setExecutorService(ExecutorService executorService) {
		watcher.setExecutorService(executorService);
		return this;
	}

	@Override
	public DirectoryWatcherFactory ifNewFile(Consumer<Path> consumer) {
		watcher.onNewFile(consumer);
		return this;
	}

	@Override
	public DirectoryWatcherFactory ifUpdatedFile(Consumer<Path> consumer) {
		watcher.onModifiedFile(consumer);
		return this;
	}

	@Override
	public DirectoryWatcherFactory ifDeletedFile(Consumer<Path> consumer) {
		watcher.onDeletedFile(consumer);
		return this;
	}

	@Override
	public DirectoryWatcherFactory addFileHandler(FileHandler handler) {
		watcher.addFileHandler(handler);
		return this;
	}

	@Override
	public DirectoryWatcher get() {
		return watcher;
	}
}
