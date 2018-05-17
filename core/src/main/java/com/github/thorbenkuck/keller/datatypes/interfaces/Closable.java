package com.github.thorbenkuck.keller.datatypes.interfaces;

public interface Closable {

	void close();

	void open();

	boolean isClosed();

	void assertIsOpen();

	default void ifClosed(Runnable runnable) {
		if(isClosed()) {
			runnable.run();
		}
	}

	default void ifOpen(Runnable runnable) {
		if(!isClosed()) {
			runnable.run();
		}
	}
}
