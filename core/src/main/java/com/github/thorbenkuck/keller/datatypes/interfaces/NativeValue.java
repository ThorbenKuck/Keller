package com.github.thorbenkuck.keller.datatypes.interfaces;

import com.github.thorbenkuck.keller.utility.Keller;

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
		Keller.parameterNotNull(t);
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
		return "Value{" + (t == null ? "empty" : t) + "}";
	}
}
