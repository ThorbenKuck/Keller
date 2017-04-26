package de.thorbenkuck.keller.datatypes.interfaces;

@FunctionalInterface
public interface Acceptor<T> {
	void accept(T t);
}
