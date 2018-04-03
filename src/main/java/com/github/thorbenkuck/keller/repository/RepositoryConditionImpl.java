package com.github.thorbenkuck.keller.repository;

import java.util.function.Predicate;

final class RepositoryConditionImpl<T> implements RepositoryCondition<T> {

	private final ActionStack<T> actionStack;

	public RepositoryConditionImpl(final ActionStack<T> actionStack) {
		this.actionStack = actionStack;
	}

	@Override
	public RepositoryConditionConnection<T> objectFulfills(final Predicate<T> predicate) {
		actionStack.addPredicate(predicate);
		return new RepositoryConditionConnectionImpl<>(actionStack);
	}

	@Override
	public RepositoryConditionConnection<T> objectDoesNotMeet(final Predicate<T> predicate) {
		actionStack.addPredicate(t -> !predicate.test(t));
		return new RepositoryConditionConnectionImpl<>(actionStack);
	}
}
