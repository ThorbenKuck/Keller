package de.thorbenkuck.keller.datatypes.interfaces;

@FunctionalInterface
public interface Factory<T> {
	T produce();
}
