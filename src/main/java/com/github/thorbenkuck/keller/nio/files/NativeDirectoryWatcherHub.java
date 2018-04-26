package com.github.thorbenkuck.keller.nio.files;

import java.nio.file.Path;
import java.util.*;

final class NativeDirectoryWatcherHub implements DirectoryWatcherHub {

	private final Map<Path, DirectoryWatcher> hashMap = new HashMap<>();

	@Override
	public final void addDirectoryWatcher(Path path) throws DirectoryWatcherException {
		DirectoryWatcher directoryWatcher = new NativeDirectoryWatcher();
		try {
			directoryWatcher.watch(path);
		} catch (DirectoryWatcherException e) {
			throw new DirectoryWatcherException(e);
		}
		synchronized (hashMap) {
			hashMap.put(path, directoryWatcher);
		}
	}

	@Override
	public final Optional<DirectoryWatcher> remove(Path path) {
		if(!isSet(path)) {
			return Optional.empty();
		}

		DirectoryWatcher watcher;
		synchronized (hashMap) {
			watcher = hashMap.remove(path);
		}
		if(watcher == null) {
			throw new ConcurrentModificationException("Hub was modified while calling remove");
		}
		return Optional.of(watcher);
	}

	@Override
	public final boolean isSet(Path path) {
		synchronized (hashMap) {
			return hashMap.get(path) != null;
		}
	}

	@Override
	public final boolean isEmpty() {
		synchronized (hashMap) {
			return hashMap.isEmpty();
		}
	}

	@Override
	public final List<DirectoryWatcher> shutDown() {
		final List<DirectoryWatcher> returnValue = new ArrayList<>();
		synchronized (hashMap) {
			for(Path path : hashMap.keySet()) {
				DirectoryWatcher watcher = hashMap.remove(path);
				watcher.stopWatching();
				returnValue.add(watcher);
			}

		}
		return returnValue;
	}
}
