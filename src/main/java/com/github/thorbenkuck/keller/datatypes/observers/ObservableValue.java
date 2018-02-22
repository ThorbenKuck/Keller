package com.github.thorbenkuck.keller.datatypes.observers;

public interface ObservableValue<T> {

	static <T> ObservableValue<T> of(T t) {
		return new GenericObservableValue<>(t);
	}

	void set(T t);

	T get();

	void addObserver(ValueListener<T> genericObserver);

	boolean deleteObserver(ValueListener<T> genericObserver);

	void deleteObservers();

	boolean hasChanged();

	int countObservers();

	void update();

}
