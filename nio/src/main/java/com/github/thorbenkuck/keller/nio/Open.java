package com.github.thorbenkuck.keller.nio;

import java.io.IOException;
import java.nio.file.Path;

public interface Open {

	void open(String path) throws IOException;

	void open(Path path) throws IOException;

}
