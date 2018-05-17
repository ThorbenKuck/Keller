package com.github.thorbenkuck.keller.nio.files;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface DirectoryWatcherHub {

	static DirectoryWatcherHubFactory build() {
		return new NativeDirectoryWatcherHubFactory();
	}

	static DirectoryWatcherHub empty() {
		return build().empty();
	}

	void addDirectoryWatcher(Path path) throws DirectoryWatcherException;

	Optional<DirectoryWatcher> remove(Path path);

	boolean isSet(Path path);

	boolean isEmpty();

	List<DirectoryWatcher> shutDown();

}
