package com.github.thorbenkuck.keller.observers;

public interface GenericObservable<T> {

	void addObserver(final GenericObserver<T> genericObserver);

	boolean deleteObserver(final GenericObserver<T> genericObserver);

	void deleteObservers();

	boolean hasChanged();

	int countObservers();
}
