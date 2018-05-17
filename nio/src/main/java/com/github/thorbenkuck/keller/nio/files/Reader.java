package com.github.thorbenkuck.keller.nio.files;

import com.github.thorbenkuck.keller.nio.Close;

import java.io.IOException;
import java.util.function.Consumer;

public interface Reader extends Close {

	void read() throws IOException;

	void read(final StringBuilder buffer) throws IOException;

	void processLine(final Consumer<Line> consumer);

	void stopProcessing(Consumer<Line> consumer);
}
