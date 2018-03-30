package com.github.thorbenkuck.keller.observers;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;

public interface ObservableValue<T> extends Value<T> {

	static <T> ObservableValue<T> of(T t) {
		return new GenericObservableValue<>(t);
	}

	void set(T t);

	void addObserver(ValueListener<T> genericObserver);

	boolean deleteObserver(ValueListener<T> genericObserver);

	void deleteObservers();

	boolean hasChanged();

	int countObservers();

	void update();

}
