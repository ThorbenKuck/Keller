package com.github.thorbenkuck.keller.repository;

public interface Repository {

	static Repository instantiate() {
		return new ListingRepository();
	}

	boolean clear();

	boolean isEmpty();

	void put(Object object);

	<T> ConditionalSupplying<T> access(Class<T> clazz);

}
