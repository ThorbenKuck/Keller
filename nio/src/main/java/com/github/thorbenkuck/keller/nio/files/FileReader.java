package com.github.thorbenkuck.keller.nio.files;

import com.github.thorbenkuck.keller.nio.Open;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public interface FileReader extends Open, Reader {

	static FileReader create() {
		return new NativeFileReader();
	}

	static Reader at(String path) throws IOException {
		return at(Paths.get(path));
	}

	static Reader at(Path path) throws IOException {
		FileReader reader = create();
		reader.open(path);
		return reader;
	}
}
