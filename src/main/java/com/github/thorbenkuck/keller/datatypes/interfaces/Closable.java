package com.github.thorbenkuck.keller.datatypes.interfaces;

public interface Closable {

	void close();

	void open();

	boolean isClosed();

	void assertIsOpen();

}
