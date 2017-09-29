package de.thorbenkuck.keller.repository;

class WayPointImpl<T> implements WayPoint<T> {

	private ActionStack<T> actionStack;

	WayPointImpl(ActionStack<T> actionStack) {
		this.actionStack = actionStack;
	}

	@Override
	public Finalizer<T> otherwise() {
		return null;
	}

	@Override
	public void now() {
		// Look if present, if not, run ifNotPresent
	}

	@Override
	public NotPresentHandler<T> andThen() {
		return new NotPresentHandlerImpl<>(actionStack);
	}
}
