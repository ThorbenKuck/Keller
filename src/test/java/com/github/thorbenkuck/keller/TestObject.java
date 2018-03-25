package com.github.thorbenkuck.keller;

public class TestObject {

	private int value;

	public TestObject() {
		this(0);
	}

	public TestObject(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int i) {
		value = i;
	}

	@Override
	public String toString() {
		return "TestObject{" + value + "}";
	}
}
