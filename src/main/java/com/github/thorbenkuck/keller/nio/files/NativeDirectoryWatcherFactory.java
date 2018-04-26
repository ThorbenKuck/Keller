package com.github.thorbenkuck.keller.nio.files;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

final class NativeDirectoryWatcherFactory implements DirectoryWatcherFactory {

	private final NativeDirectoryWatcher watcher = new NativeDirectoryWatcher();

	@Override
	public final DirectoryWatcherFactory setExecutorService(ExecutorService executorService) {
		watcher.setExecutorService(executorService);
		return this;
	}

	@Override
	public final DirectoryWatcherFactory ifNewFile(Consumer<Path> consumer) {
		watcher.onNewFile(consumer);
		return this;
	}

	@Override
	public final DirectoryWatcherFactory ifUpdatedFile(Consumer<Path> consumer) {
		watcher.onModifiedFile(consumer);
		return this;
	}

	@Override
	public final DirectoryWatcherFactory ifDeletedFile(Consumer<Path> consumer) {
		watcher.onDeletedFile(consumer);
		return this;
	}

	@Override
	public final DirectoryWatcherFactory addFileHandler(FileHandler handler) {
		watcher.addFileHandler(handler);
		return this;
	}

	@Override
	public final DirectoryWatcher get() {
		return watcher;
	}
}
