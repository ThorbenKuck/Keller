package com.github.thorbenkuck.keller.nio.files;

import com.github.thorbenkuck.keller.nio.Open;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public interface FileWriter extends Open, Writer {

	static FileWriter create() {
		return new NativeFileWriter();
	}

	static Writer at(String path) throws IOException {
		return at(Paths.get(path));
	}

	static Writer at(Path path) throws IOException {
		FileWriter fileWriter = create();
		fileWriter.open(path);
		return fileWriter;
	}
}
