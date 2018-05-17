package com.github.thorbenkuck.keller.nio.files;

import com.github.thorbenkuck.keller.nio.Close;

import java.io.IOException;

public interface Writer extends Close {

	void write(String string) throws IOException;

	void set(String string) throws IOException;

	void append(String string) throws IOException;

	void clear() throws IOException;

	void newLine() throws IOException;

}
