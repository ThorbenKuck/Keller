package de.thorbenkuck.keller.repository;

import java.util.Collection;
import java.util.Random;

public class SupplyingImpl<T> implements Supplying<T> {

	private ActionStack<T> actionStack;

	public SupplyingImpl(ActionStack<T> actionStack) {
		this.actionStack = actionStack;
	}

	@Override
	public T getFirst() {
		return actionStack.getPrimaryMatchingElement();
	}

	@Override
	public T getAny() {
		return actionStack.getMatchingObjects().get(new Random(12300123).nextInt(actionStack.getMatchingObjects().size()));
	}

	@Override
	public Collection<T> getAll() {
		return actionStack.getMatchingObjects();
	}

	@Override
	public NotPresentHandler<T> ifNotPresent() {
		return new NotPresentHandlerImpl<>(actionStack);
	}
}
