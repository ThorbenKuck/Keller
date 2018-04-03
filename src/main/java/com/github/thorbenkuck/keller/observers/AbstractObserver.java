package com.github.thorbenkuck.keller.observers;

public abstract class AbstractObserver<T> implements GenericObserver<T> {

	private final Class<T> tClass;

	protected AbstractObserver() {
		this(null);
	}

	protected AbstractObserver(final Class<T> tClass) {
		this.tClass = tClass;
	}

	public final boolean accept(final T t) {
		return tClass == null || tClass.equals(t);
	}
}
