package com.github.thorbenkuck.keller.repository;

import com.github.thorbenkuck.keller.datatypes.interfaces.QueuedAction;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

class NotPresentHandlerImpl<T> implements NotPresentHandler<T> {

	private ActionStack<T> tActionStack;

	NotPresentHandlerImpl(ActionStack<T> tActionStack) {
		this.tActionStack = tActionStack;
	}

	@Override
	public WayPoint<T> throwException(RuntimeException e) {
		Objects.requireNonNull(e);
		tActionStack.addIfNotPresent(() -> {throw e;});
		return new WayPointImpl<>(tActionStack);
	}

	@Override
	public WayPoint<T> throwError(Error error) {
		Objects.requireNonNull(error);
		tActionStack.addIfNotPresent(() -> {throw error;});
		return new WayPointImpl<>(tActionStack);
	}

	@Override
	public WayPoint<T> getNullObject(Supplier<T> t) {
		Objects.requireNonNull(t);
		tActionStack.addIfNotPresent(() -> tActionStack.setPrimaryMatchingElement(t.get()));
		return new WayPointImpl<>(tActionStack);
	}

	@Override
	public WayPoint<T> run(Runnable runnable) {
		Objects.requireNonNull(runnable);
		tActionStack.addIfNotPresent(runnable);
		return new WayPointImpl<>(tActionStack);
	}

	@Override
	public WayPoint<T> run(QueuedAction queuedAction) {
		Objects.requireNonNull(queuedAction);
		tActionStack.addIfNotPresent(() -> {
			queuedAction.doBefore();
			queuedAction.doAction();
			queuedAction.doAfter();
		});
		return new WayPointImpl<>(tActionStack);
	}

	@Override
	public WayPoint<T> handleAllOfSameType(Consumer<Collection<T>> consumer) {
		Objects.requireNonNull(consumer);
		tActionStack.addIfNotPresent(() -> {
			consumer.accept(tActionStack.getMatchingObjects());
		});
		return new WayPointImpl<>(tActionStack);
	}
}
