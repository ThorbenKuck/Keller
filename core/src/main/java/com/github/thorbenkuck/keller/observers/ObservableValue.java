package com.github.thorbenkuck.keller.observers;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.keller.utility.Keller;

public interface ObservableValue<T> extends Value<T> {

	static <T> ObservableValue<T> of(final T t) {
		Keller.parameterNotNull(t);
		return new NativeObservableValue<>(t);
	}

	static <T> ObservableValue<T> empty() {
		return new NativeObservableValue<>();
	}

	void addObserver(final ValueListener<T> genericObserver);

	boolean deleteObserver(final ValueListener<T> genericObserver);

	void deleteObservers();

	boolean hasChanged();

	int countObservers();

	void update();
}
