package com.github.thorbenkuck.keller.repository;

final class RepositoryConditionConnectionImpl<T> implements RepositoryConditionConnection<T> {

	private final ActionStack<T> actionStack;

	public RepositoryConditionConnectionImpl(final ActionStack<T> actionStack) {
		this.actionStack = actionStack;
	}

	@Override
	public RepositoryCondition<T> and() {
		return new RepositoryConditionImpl<>(actionStack);
	}

	@Override
	public Supplying<T> then() {
		return new SupplyingImpl<>(actionStack);
	}
}
