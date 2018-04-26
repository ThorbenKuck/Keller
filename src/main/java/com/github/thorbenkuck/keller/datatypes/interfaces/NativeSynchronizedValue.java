package com.github.thorbenkuck.keller.datatypes.interfaces;

import com.github.thorbenkuck.keller.utility.Keller;

final class NativeSynchronizedValue<T> implements Value<T> {

	private T t;

	NativeSynchronizedValue(T t) {
		this.t = t;
	}

	@Override
	public synchronized T get() {
		return t;
	}

	@Override
	public synchronized void set(T t) {
		Keller.parameterNotNull(t);
		this.t = t;
	}

	@Override
	public synchronized void clear() {
		this.t = null;
	}

	@Override
	public synchronized boolean isEmpty() {
		return t == null;
	}

	@Override
	public String toString() {
		return "SynchronizedValue{" + (t == null ? "empty" : t) + "}";
	}
}
