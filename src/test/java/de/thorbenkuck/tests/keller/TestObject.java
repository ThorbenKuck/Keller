package de.thorbenkuck.tests.keller;

class TestObject {

	private int value;

	public TestObject() {
		this(0);
	}

	public TestObject(int value) {
		this.value = value;
	}

	int getValue() {
		return value;
	}

	void setValue(int i) {
		value = i;
	}
}
