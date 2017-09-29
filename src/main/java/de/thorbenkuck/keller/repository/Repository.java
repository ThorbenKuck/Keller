package de.thorbenkuck.keller.repository;

public interface Repository {

	static Repository instantiate() {
		return null;
	}

	boolean clear();

	boolean isEmpty();

	void put(Object object);

	<T> ConditionalSupplying<T> access(Class<T> clazz);

}
