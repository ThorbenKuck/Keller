package com.github.thorbenkuck.keller.nio.files;

import com.github.thorbenkuck.keller.observers.ObservableValue;
import com.github.thorbenkuck.keller.pipe.Pipeline;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.stream.Stream;

final class NativeFileReader implements FileReader {

	private final ObservableValue<Path> target = ObservableValue.empty();
	private final Pipeline<Line> lineProcessor = Pipeline.unifiedCreation();
	private int currentLine = 0;

	private void setEmpty() {
		synchronized (target) {
			target.clear();
		}
	}

	private void set(Path newPath) {
		synchronized (target) {
			target.set(newPath);
		}
	}

	private Path get() {
		synchronized (target) {
			return target.get();
		}
	}

	private boolean pathSet() {
		return get() != null;
	}

	private void handle(String string) {
		synchronized (lineProcessor) {
			lineProcessor.apply(Line.create(currentLine++, string));
		}
	}

	private void requireReadable(final Path path) throws IOException {
		if (!Files.isRegularFile(path)) {
			throw new IOException("Set path is no file");
		}
		if (!Files.isReadable(path)) {
			throw new IOException(path.toAbsolutePath().toString() + " is not readable");
		}
	}

	@Override
	public void open(String path) throws IOException {
		open(Paths.get(path));
	}

	@Override
	public void open(Path path) throws IOException {
		if (!Files.isRegularFile(path)) {
			throw new IOException("No File: " + path);
		}
		if (pathSet()) {
			throw new IOException("Already bound to " + get());
		}
		set(path);
	}

	@Override
	public void close() {
		if (pathSet()) {
			setEmpty();
		}
	}

	@Override
	public synchronized void read() throws IOException {
		if (!pathSet()) {
			throw new IOException("No set file to read from");
		}
		final Path path = get();
		requireReadable(path);
		currentLine = 0;
		synchronized (this) {
			try (Stream<String> lines = Files.lines(path, Charset.defaultCharset())) {
				lines.forEachOrdered(this::handle);
			}
		}
	}

	@Override
	public void read(final StringBuilder stringBuilder) throws IOException {
		LocalConsumer consumer = new LocalConsumer(stringBuilder);
		processLine(consumer);
		read();
		stopProcessing(consumer);
	}

	@Override
	public void processLine(Consumer<Line> consumer) {
		synchronized (lineProcessor) {
			lineProcessor.addLast(consumer);
		}
	}

	@Override
	public void stopProcessing(Consumer<Line> consumer) {
		synchronized (lineProcessor) {
			lineProcessor.remove(consumer);
		}
	}

	private final class LocalConsumer implements Consumer<Line> {

		private final StringBuilder stringBuilder;

		public LocalConsumer(final StringBuilder stringBuilder) {

			this.stringBuilder = stringBuilder;
		}

		/**
		 * Performs this operation on the given argument.
		 *
		 * @param s the input argument
		 */
		@Override
		public void accept(Line s) {
			stringBuilder.append(s.getContent());
		}
	}
}
