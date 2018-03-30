package com.github.thorbenkuck.keller.nio.files;

import java.nio.file.Path;

public interface DirectoryWatcherHubFactory {

	DirectoryWatcherHubFactory add(Path path);

	DirectoryWatcherHub initialize() throws DirectoryWatcherException;

	DirectoryWatcherHub empty();

}
