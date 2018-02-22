package com.github.thorbenkuck.keller.datatypes.observers;

public interface GenericObservable<T> {

	void addObserver(GenericObserver<T> genericObserver);

	boolean deleteObserver(GenericObserver<T> genericObserver);

	void deleteObservers();

	boolean hasChanged();

	int countObservers();
}
