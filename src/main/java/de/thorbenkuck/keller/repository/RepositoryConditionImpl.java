package de.thorbenkuck.keller.repository;

import java.util.function.Predicate;

public class RepositoryConditionImpl<T> implements RepositoryCondition<T> {

	private ActionStack<T> actionStack;

	public RepositoryConditionImpl(ActionStack<T> actionStack) {
		this.actionStack = actionStack;
	}

	@Override
	public RepositoryConditionConnection<T> objectFulfills(Predicate<T> predicate) {
		actionStack.addPredicate(predicate);
		return new RepositoryConditionConnectionImpl<T>(actionStack);
	}

	@Override
	public RepositoryConditionConnection<T> objectDoesNotMeet(Predicate<T> predicate) {
		actionStack.addPredicate(t -> !predicate.test(t));
		return new RepositoryConditionConnectionImpl<T>(actionStack);
	}
}
