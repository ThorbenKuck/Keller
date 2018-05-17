package com.github.thorbenkuck.keller.repository;

class WayPointImpl<T> implements WayPoint<T> {

	private final ActionStack<T> actionStack;

	WayPointImpl(final ActionStack<T> actionStack) {
		this.actionStack = actionStack;
	}

	@Override
	public Finalizer<T> otherwise() {
		return new FinalizerImpl<>(actionStack);
	}

	@Override
	public void now() {
		new FinalizerImpl<>(actionStack).getAny();
	}

	@Override
	public NotPresentHandler<T> andThen() {
		return new NotPresentHandlerImpl<>(actionStack);
	}
}
