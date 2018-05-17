package com.github.thorbenkuck.keller.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class FinalizerImpl<T> implements Finalizer<T> {

	private final RepositoryInternals repositoryInternals;
	private final ActionStack<T> actionStack;

	public FinalizerImpl(final ActionStack<T> actionStack) {
		this.repositoryInternals = actionStack.getInternals();
		this.actionStack = actionStack;
	}

	@Override
	public T getFirst() {
		final Optional<Object> optional = getFilteredStream().findFirst();
		return handle(optional);
	}

	@Override
	public T getAny() {
		final Optional<Object> optional = getFilteredStream().findAny();
		return handle(optional);
	}
	
	private T handle(final Optional<Object> objectOptional) {
		if(objectOptional.isPresent()) {
			actionStack.getIfPresent().forEach(Runnable::run);
			return (T) objectOptional.get();
		} else {
			actionStack.getIfNotPresent().forEach(Runnable::run);
			return actionStack.getNullObject();
		}
	}

	@Override
	public Collection<T> getAll() {
		final List<Object> objects = getFilteredStream().collect(Collectors.toCollection(ArrayList::new));
		return (Collection<T>) objects;
	}

	@Override
	public T run(final Consumer<T> consumer) {
		return null;
	}

	@Override
	public T run(final Runnable runnable) {
		return null;
	}

	private Stream<Object> getFilteredStream() {
		return repositoryInternals.stream()
				.filter(o -> o.getClass().equals(actionStack.getType()))
				.filter(o -> actionStack.getPredicateList().stream()
						.filter(predicate -> predicate.test((T) o))
						.count() == actionStack.getPredicateList().size());
	}
}
