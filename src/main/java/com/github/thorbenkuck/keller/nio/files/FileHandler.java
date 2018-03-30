package com.github.thorbenkuck.keller.nio.files;

import java.nio.file.Path;

public interface FileHandler {

	default void updatedFile(Path path) {}

	default void deletedFile(Path path) {}

	void newFile(Path path);

}
