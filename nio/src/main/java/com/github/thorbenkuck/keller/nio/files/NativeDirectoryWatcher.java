package com.github.thorbenkuck.keller.nio.files;

import com.github.thorbenkuck.keller.pipe.Pipeline;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

final class NativeDirectoryWatcher implements DirectoryWatcher {

	private ExecutorService executorService = Executors.newSingleThreadExecutor();
	private final AtomicReference<PathWatcher> watcher = new AtomicReference<>();
	private Consumer<Exception> exceptionConsumer = e -> e.printStackTrace(System.out);
	private final Pipeline<Path> newFilePipeline = Pipeline.unifiedCreation();
	private final Pipeline<Path> updatedFilePipeline = Pipeline.unifiedCreation();
	private final Pipeline<Path> deletedFilePipeline = Pipeline.unifiedCreation();

	private void runNew(Path path) {
		newFilePipeline.apply(path);
	}

	private void runUpdated(Path path) {
		updatedFilePipeline.apply(path);
	}

	private void runDeleted(Path path) {
		deletedFilePipeline.apply(path);
	}

	private void exception(Exception consumer) {
		this.exceptionConsumer.accept(consumer);
	}

	void setExceptionConsumer(Consumer<Exception> consumer) {
		this.exceptionConsumer = consumer;
	}

	void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	@Override
	public final synchronized void onNewFile(Consumer<Path> consumer) {
		newFilePipeline.addFirst(consumer);
	}

	@Override
	public final synchronized void onModifiedFile(Consumer<Path> consumer) {
		updatedFilePipeline.addFirst(consumer);
	}

	@Override
	public final synchronized void onDeletedFile(Consumer<Path> consumer) {
		deletedFilePipeline.addFirst(consumer);
	}

	@Override
	public final void addFileHandler(FileHandler fileHandler) {
		onNewFile(fileHandler::newFile);
		onModifiedFile(fileHandler::updatedFile);
		onDeletedFile(fileHandler::deletedFile);
	}

	@Override
	public final void watch(String path) throws DirectoryWatcherException {
		watch(Paths.get(path));
	}

	@Override
	public final synchronized void watch(Path directoryPath) throws DirectoryWatcherException {
		if(!Files.exists(directoryPath) || !Files.isDirectory(directoryPath)) {
			throw new DirectoryWatcherException("Can only watch directorys! Directory expected!");
		}

		final Semaphore start = new Semaphore(1);

		stopWatching();
		PathWatcher pathWatcher = new PathWatcher(directoryPath, this::runNew, this::runUpdated, this::runDeleted, this::exception, start::release);

		synchronized (watcher) {
			watcher.set(pathWatcher);
			executorService.submit(pathWatcher);
		}

		try {
			start.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public final void stopWatching() {
		PathWatcher runnable;
		synchronized (watcher) {
			runnable = watcher.get();
		}
		if (runnable == null) {
			return;
		}

		runnable.softStop();
	}

	@Override
	public final void close() {
		stopWatching();
		executorService.shutdown();
	}

	private final class PathWatcher implements Runnable {

		private Thread runningIn;
		private final AtomicBoolean running = new AtomicBoolean(false);
		private final Path folder;
		private final Consumer<Path> newFile;
		private final Consumer<Path> updatedFile;
		private final Consumer<Path> deletedFile;
		private final Consumer<Exception> exceptionConsumer;
		private final Runnable startedRunnable;

		private PathWatcher(Path folder, Consumer<Path> newFile, Consumer<Path> updatedFile, Consumer<Path> deletedFile,
		                    Consumer<Exception> exceptionConsumer, Runnable startedRunnable) {
			this.folder = folder;
			this.newFile = newFile;
			this.updatedFile = updatedFile;
			this.deletedFile = deletedFile;
			this.exceptionConsumer = exceptionConsumer;
			this.startedRunnable = startedRunnable;
		}

		private void handle(Exception e) {
			exceptionConsumer.accept(e);
		}

		/**
		 * When an object implementing interface <code>Runnable</code> is used
		 * to create a thread, starting the thread causes the object's
		 * <code>run</code> method to be called in that separately executing
		 * thread.
		 * <p>
		 * The general contract of the method <code>run</code> is that it may
		 * take any action whatsoever.
		 *
		 * @see Thread#run()
		 */
		@Override
		public final void run() {
			try(WatchService watchService = FileSystems.getDefault().newWatchService()) {
				running.set(true);
				runningIn = Thread.currentThread();
				startedRunnable.run();
				folder.register(watchService,
						StandardWatchEventKinds.ENTRY_CREATE,
						StandardWatchEventKinds.ENTRY_MODIFY,
						StandardWatchEventKinds.ENTRY_DELETE,
						StandardWatchEventKinds.OVERFLOW);
				while(running.get()) {
					watch(watchService);
				}
			} catch (IOException e) {
				handle(e);
			}

		}

		private void watch(WatchService watchService) {
			WatchKey watchKey;
			try {
				watchKey = watchService.take();
			} catch (InterruptedException e) {
				if(running.get()) {
					handle(e);
				}
				return;
			}

			System.out.println("Received watchEvent");

			for(WatchEvent watchEvent : watchKey.pollEvents()) {
				handleEvent(watchEvent);
			}

			if(!watchKey.reset()) {
				running.set(false);
			}
		}

		private void handleEvent(WatchEvent watchEvent) {
			WatchEvent.Kind<?> kind = watchEvent.kind();
			if(StandardWatchEventKinds.OVERFLOW == kind) {
				return;
			}

			Path path = folder.resolve(((WatchEvent<Path>) watchEvent).context());
			if(StandardWatchEventKinds.ENTRY_CREATE == kind) {
				newFile.accept(path);
			} else if(StandardWatchEventKinds.ENTRY_MODIFY == kind) {
				updatedFile.accept(path);
			} else if(StandardWatchEventKinds.ENTRY_DELETE == kind) {
				deletedFile.accept(path);
			}
		}

		public void softStop() {
			running.set(false);
			if(runningIn != null) {
				runningIn.interrupt();
			}
		}
	}
}
