package de.thorbenkuck.keller.datatypes.interfaces;

@FunctionalInterface
public interface Handler<T, D> {
	void handle(T t, D d);
}
