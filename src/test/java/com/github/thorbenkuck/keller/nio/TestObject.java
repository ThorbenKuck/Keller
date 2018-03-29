package com.github.thorbenkuck.keller.nio;

import java.io.Serializable;

public class TestObject implements Serializable {

	private final String string;

	public TestObject(String string) {
		this.string = string;
	}

	public String getString() {
		return string;
	}

	@Override
	public String toString() {
		return "TestObject{" + getString() + "}";
	}
}
