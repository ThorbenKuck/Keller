package com.github.thorbenkuck.keller.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

final class ConditionalSupplyingImpl<T> implements ConditionalSupplying<T> {

	private final RepositoryInternals repositoryInternals;
	private final Class<T> clazz;
	private final ActionStack<T> actionStack;

	ConditionalSupplyingImpl(final RepositoryInternals repositoryInternals, final Class<T> clazz) {
		this(repositoryInternals, clazz, new ActionStack<>(repositoryInternals, clazz));
	}

	ConditionalSupplyingImpl(final RepositoryInternals repositoryInternals, final Class<T> clazz, final ActionStack<T> actionStack) {
		this.repositoryInternals = repositoryInternals;
		this.clazz = clazz;
		this.actionStack = actionStack;
	}

	@Override
	public T getFirst() {
		final Optional<Object> toReturn = repositoryInternals.stream()
				.filter(o -> o.getClass().equals(clazz))
				.findFirst();
		return (T) toReturn.orElse(null);
	}

	@Override
	public T getAny() {
		final Optional<Object> toReturn = repositoryInternals.stream()
				.filter(o -> o.getClass().equals(clazz))
				.findAny();
		return (T) toReturn.orElse(null);
	}

	@Override
	public Collection<T> getAll() {
		final Collection<Object> toReturn = new ArrayList<>();
		repositoryInternals.stream()
				.filter(o -> o.getClass().equals(clazz))
				.forEach(toReturn::add);
		return (Collection<T>) toReturn;
	}

	@Override
	public NotPresentHandler<T> ifNotPresent() {
		return new NotPresentHandlerImpl<>(actionStack);
	}

	@Override
	public RepositoryCondition<T> withRequirement() {
		return new RepositoryConditionImpl<>(actionStack);
	}
}
