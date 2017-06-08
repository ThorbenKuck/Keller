package de.thorbenkuck.keller.datatypes.interfaces;

@FunctionalInterface
public interface BiGenericRunnable<T, U> {

	void run(T t, U u);

}
