package com.github.thorbenkuck.keller.sync;

import com.github.thorbenkuck.keller.datatypes.interfaces.QueuedAction;
import com.github.thorbenkuck.keller.datatypes.interfaces.Value;

final class NativeCountDownLatchSynchronize extends AbstractCountDownLatchSynchronize {

	private Value<QueuedAction> onError = Value.synchronize(() -> System.out.println(this + " encountered an error!"));

	NativeCountDownLatchSynchronize() {
		this(1);
	}

	NativeCountDownLatchSynchronize(final int numberOfActions) {
		super(numberOfActions);
	}

	/**
	 * This Method will call the Runnable, set via {@link #setOnError(QueuedAction)}.
	 * {@inheritDoc}
	 */
	@Override
	public void error() {
		QueuedAction.call(onError);
	}

	/**
	 * Sets an Runnable, that should be executed if an error occurred
	 *
	 * @param action the runnable, that should be executed.
	 */
	@Override
	public void setOnError(QueuedAction action) {
		onError.set(action);
	}
}
