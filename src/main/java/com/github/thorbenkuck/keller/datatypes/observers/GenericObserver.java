package com.github.thorbenkuck.keller.datatypes.observers;

import java.util.function.BiConsumer;

public interface GenericObserver<T> {

	static <T> GenericObserver<T> of(Class<T> clazz, BiConsumer<T, AbstractGenericObservable> consumer) {
		return new WrappingObserver<>(clazz, consumer);
	}

	void update(T t, AbstractGenericObservable abstractGenericObservable);

	default boolean accepts(Object object) {
		return true;
	}


}
