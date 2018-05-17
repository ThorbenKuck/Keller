package com.github.thorbenkuck.keller.observers;

import java.util.function.BiConsumer;

public interface GenericObserver<T> {

	static <T> GenericObserver<T> of(final Class<T> clazz, final BiConsumer<T, AbstractGenericObservable> consumer) {
		return new WrappingObserver<>(clazz, consumer);
	}

	void update(final T t, final AbstractGenericObservable abstractGenericObservable);

	default boolean accepts(final Object object) {
		return true;
	}


}
