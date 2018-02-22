package com.github.thorbenkuck.keller.datatypes.observers;

public interface ValueListener<T> {

	void onChange(T t, ObservableValue<T> source);

}
