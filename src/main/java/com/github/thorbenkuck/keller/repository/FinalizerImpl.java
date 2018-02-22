package com.github.thorbenkuck.keller.repository;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class FinalizerImpl<T> implements Finalizer<T> {

	private RepositoryInternals repositoryInternals;
	private ActionStack<T> actionStack;

	public FinalizerImpl(ActionStack<T> actionStack) {
		this.repositoryInternals = actionStack.getInternals();
		this.actionStack = actionStack;
	}

	@Override
	public T getFirst() {
		Optional<Object> optional = getFilteredStream().findFirst();
		return handle(optional);
	}

	@Override
	public T getAny() {
		Optional<Object> optional = getFilteredStream().findAny();
		return handle(optional);
	}
	
	private T handle(Optional<Object> objectOptional) {
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
//		List<Object> objects = getFilteredStream().collect(Collectors.toCollection(ArrayList::new));
		return null;
	}

	@Override
	public T run(Consumer<T> consumer) {
		return null;
	}

	@Override
	public T run(Runnable runnable) {
		return null;
	}

	private Stream getFilteredStream() {
		return repositoryInternals.stream()
				.filter(o -> o.getClass().equals(actionStack.getClass()))
				.filter(o -> actionStack.getPredicateList().stream()
						.filter(predicate -> predicate.test((T) o))
						.count() == actionStack.getPredicateList().size());
	}
}
