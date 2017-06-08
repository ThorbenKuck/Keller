package de.thorbenkuck.keller.datatypes.interfaces;

import de.thorbenkuck.keller.datatypes.GenericObservable;

public interface GenericObserver<T> {

	void update(T t, GenericObservable genericObservable);

	default boolean accept(T t) {
		return true;
	}

}
