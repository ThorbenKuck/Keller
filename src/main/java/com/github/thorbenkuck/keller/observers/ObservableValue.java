package com.github.thorbenkuck.keller.observers;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.keller.utility.Keller;

public interface ObservableValue<T> extends Value<T> {

	static <T> ObservableValue<T> of(T t) {
		Keller.parameterNotNull(t);
		return new GenericObservableValue<>(t);
	}

	static <T> ObservableValue<T> empty() {
		return new GenericObservableValue<>();
	}

	void set(T t);

	void addObserver(ValueListener<T> genericObserver);

	boolean deleteObserver(ValueListener<T> genericObserver);

	void deleteObservers();

	boolean hasChanged();

	boolean isEmpty();

	int countObservers();

	void update();

}
