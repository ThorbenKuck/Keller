package com.github.thorbenkuck.keller.datatypes.interfaces;

final class NativeValue<T> implements Value<T> {

	private T t;

	NativeValue(T t) {
		this.t = t;
	}

	@Override
	public T get() {
		return t;
	}

	@Override
	public void set(T t) {
		this.t = t;
	}

	@Override
	public void clear() {
		this.t = null;
	}

	@Override
	public boolean isEmpty() {
		return t == null;
	}

	@Override
	public String toString() {
		return "Value{" + t + "}";
	}
}
