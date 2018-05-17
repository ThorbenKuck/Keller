package com.github.thorbenkuck.keller.datatypes.interfaces;

@FunctionalInterface
public interface GenericRunnable<T> {

	void run(T t);

}
