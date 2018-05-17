package com.github.thorbenkuck.keller.repository;

final class ListingRepository implements Repository {

	private final RepositoryInternals repositoryInternals = new RepositoryInternals();

	@Override
	public final boolean clear() {
		return repositoryInternals.clear();
	}

	@Override
	public final boolean isEmpty() {
		return repositoryInternals.isEmpty();
	}

	@Override
	public final void put(final Object object) {
		repositoryInternals.add(object);
	}

	@Override
	public final <T> ConditionalSupplying<T> access(Class<T> clazz) {
		return new ConditionalSupplyingImpl<>(repositoryInternals, clazz);
	}
}
