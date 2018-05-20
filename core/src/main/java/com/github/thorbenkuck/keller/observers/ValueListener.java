package com.github.thorbenkuck.keller.observers;

@FunctionalInterface
public interface ValueListener<T> {

	void onChange(final T t, final ObservableValue<T> source);

}
