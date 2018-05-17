package com.github.thorbenkuck.keller.observers;

public interface ValueListener<T> {

	void onChange(final T t, final ObservableValue<T> source);

}
