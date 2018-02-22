package com.github.thorbenkuck.keller.datatypes.interfaces;

import com.github.thorbenkuck.keller.datatypes.GenericObservable;

public interface GenericObserver<T> {

	void update(T t, GenericObservable genericObservable);

	default boolean accept(T t) {
		return true;
	}

}
