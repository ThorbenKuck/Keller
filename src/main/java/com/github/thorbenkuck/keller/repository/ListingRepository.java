package com.github.thorbenkuck.keller.repository;

public class ListingRepository implements Repository {

	private RepositoryInternals repositoryInternals = new RepositoryInternals();

	@Override
	public boolean clear() {
		return repositoryInternals.clear();
	}

	@Override
	public boolean isEmpty() {
		return repositoryInternals.isEmpty();
	}

	@Override
	public void put(Object object) {
		repositoryInternals.add(object);
	}

	@Override
	public <T> ConditionalSupplying<T> access(Class<T> clazz) {
		return new ConditionalSupplyingImpl<>(repositoryInternals, clazz);
	}
}
