package com.github.thorbenkuck.keller.datatypes.interfaces;

import com.github.thorbenkuck.keller.utility.Keller;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface Value<T> extends Updatable<T>, Readable<T> {

	static <T> Value<T> of(T t) {
		return new NativeValue<>(t);
	}

	static <T> Value<T> synchronize(T t) { return new NativeSynchronizedValue<>(t); }

	static <T> Value<T> empty() {
		return new NativeValue<>(null);
	}

	static <T> Value<T> emptySynchronized() { return new NativeSynchronizedValue<>(null); }

	static <T> Readable<T> readOnly(T t) {
		return of(t);
	}

	static <T> Readable<T> readOnlySynchronized(T t) {
		return synchronize(t);
	}

	default void requireNotEmpty() {
		if(isEmpty()) {
			throw new IllegalStateException("The Value " + this + " is required to not be empty!");
		}
	}

	default void set(Supplier<T> supplier) {
		Keller.parameterNotNull(supplier);
		set(supplier.get());
	}

	default void ifEmpty(Supplier<T> supplier) {
		Keller.parameterNotNull(supplier);
		if(isEmpty()) {
			set(supplier);
		}
	}

	default void ifNotEmpty(Consumer<T> consumer) {
		Keller.parameterNotNull(consumer);
		if(!isEmpty()) {
			consumer.accept(get());
		}
	}
}
