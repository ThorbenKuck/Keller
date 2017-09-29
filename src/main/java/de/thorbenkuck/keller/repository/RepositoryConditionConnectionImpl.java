package de.thorbenkuck.keller.repository;

public class RepositoryConditionConnectionImpl<T> implements RepositoryConditionConnection<T> {

	private ActionStack<T> actionStack;

	public RepositoryConditionConnectionImpl(ActionStack<T> actionStack) {
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
