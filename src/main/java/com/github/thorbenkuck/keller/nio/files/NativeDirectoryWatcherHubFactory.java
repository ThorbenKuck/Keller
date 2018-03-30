package com.github.thorbenkuck.keller.nio.files;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class NativeDirectoryWatcherHubFactory implements DirectoryWatcherHubFactory {

	final List<Path> pathList = new ArrayList<>();

	@Override
	public DirectoryWatcherHubFactory add(Path path) {
		pathList.add(path);
		return this;
	}

	@Override
	public DirectoryWatcherHub initialize() throws DirectoryWatcherException {
		final DirectoryWatcherHub hub = new NativeDirectoryWatcherHub();
		for(Path path : pathList) {
			hub.addDirectoryWatcher(path);
		}
		return hub;
	}

	@Override
	public DirectoryWatcherHub empty() {
		return new NativeDirectoryWatcherHub();
	}
}
