package de.thorbenkuck.keller.repository;

import java.util.Collection;

public class SupplyingImpl<T> implements Supplying<T> {
	@Override
	public T getFirst() {
		return null;
	}

	@Override
	public T getAny() {
		return null;
	}

	@Override
	public Collection<T> getAll() {
		return null;
	}

	@Override
	public NotPresentHandler<T> ifNotPresent() {
		return null;
	}
}
