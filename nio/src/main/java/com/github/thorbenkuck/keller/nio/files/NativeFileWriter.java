package com.github.thorbenkuck.keller.nio.files;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.keller.utility.Keller;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

final class NativeFileWriter implements FileWriter {

	private final Value<Path> target = Value.empty();
	private final Lock writeLock = new ReentrantLock();

	private void set(Path path) {
		synchronized (target) {
			target.set(path);
		}
	}

	private Path get() {
		synchronized (target) {
			return target.get();
		}
	}

	private Path getChecked() throws IOException {
		if (!isSet()) {
			throw new IOException("Not open");
		}

		return get();
	}

	private boolean isSet() {
		return get() != null;
	}

	private void requireWritable(final Path path) throws IOException {
		if (!Files.isRegularFile(path)) {
			throw new IOException("Set target is no file");
		}
		if (!Files.isWritable(path)) {
			throw new IOException(path.toAbsolutePath().toString() + " is not writable");
		}
	}

	private void clearTarget() {
		synchronized (target) {
			target.clear();
		}
	}

	@Override
	public final void open(String path) throws IOException {
		open(Paths.get(path));
	}

	@Override
	public final void open(Path path) throws IOException {
		if (isSet()) {
			throw new IOException("Already bound to " + get());
		}
		if (!Files.isRegularFile(path)) {
			throw new IOException("Could not locate " + path.getFileName());
		}
		requireWritable(path);
		set(path);
	}

	@Override
	public final void close() {
		if (isSet()) {
			clearTarget();
		}
	}

	@Override
	public final void set(String string) throws IOException {
		Keller.parameterNotNull(string);
		doWrite(string, StandardOpenOption.WRITE);
	}

	@Override
	public final void write(String string) throws IOException {
		set(string);
	}

	@Override
	public final void append(String string) throws IOException {
		Keller.parameterNotNull(string);
		doWrite(string, StandardOpenOption.APPEND);
	}

	@Override
	public final void newLine() throws IOException {
		append(System.lineSeparator());
	}

	@Override
	public final void clear() throws IOException {
		Path path = getChecked();
		try {
			writeLock.lock();
			try (BufferedWriter writer = Files.newBufferedWriter(path)) {
				writer.write("");
			}
		} finally {
			writeLock.unlock();
		}
	}

	private void doWrite(String what, StandardOpenOption openOption) throws IOException {
		try {
			writeLock.lock();
			Path path = getChecked();
			Files.write(path, what.getBytes(), openOption);
		} finally {
			writeLock.unlock();
		}
	}
}
