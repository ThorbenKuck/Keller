package com.github.thorbenkuck.keller.nio.files;

import java.nio.file.Path;
import java.nio.file.Paths;

public interface DirectoryWatcherHubFactory {

	default DirectoryWatcherHubFactory add(String path) {
		return add(Paths.get(path));
	}

	DirectoryWatcherHubFactory add(Path path);

	DirectoryWatcherHub initialize() throws DirectoryWatcherException;

	DirectoryWatcherHub empty();

}
