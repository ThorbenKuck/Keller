package com.github.thorbenkuck.keller.datatypes.interfaces;

public interface Value<T> {

	static <T> Value<T> of(T t) {
		return new NativeValue<>(t);
	}

	static <T> Value<T> synchronize(T t) { return new NativeSynchronizedValue<>(t); }

	static <T> Value<T> empty() {
		return new NativeValue<>(null);
	}

	static <T> Value<T> emptySynchronized() { return new NativeSynchronizedValue<>(null); }

	T get();

	void set(T t);

	void clear();

	boolean isEmpty();

}
