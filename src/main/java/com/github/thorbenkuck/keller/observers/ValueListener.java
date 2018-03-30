package com.github.thorbenkuck.keller.observers;

public interface ValueListener<T> {

	void onChange(T t, ObservableValue<T> source);

}
