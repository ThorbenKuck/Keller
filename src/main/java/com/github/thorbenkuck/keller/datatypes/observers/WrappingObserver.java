package com.github.thorbenkuck.keller.datatypes.observers;

import java.util.function.BiConsumer;

class WrappingObserver<T> extends AbstractObserver<T> {

	private final BiConsumer<T, AbstractGenericObservable> consumer;

	WrappingObserver(final Class<T> clazz, final BiConsumer<T, AbstractGenericObservable> consumer) {
		super(clazz);
		this.consumer = consumer;
	}

	@Override
	public void update(final T t, final AbstractGenericObservable abstractGenericObservable) {
		consumer.accept(t, abstractGenericObservable);
	}
}
