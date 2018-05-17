package com.github.thorbenkuck.keller.datatypes.interfaces;

@FunctionalInterface
public interface Factory<T> {
	T produce();
}
