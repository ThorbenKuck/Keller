package com.github.thorbenkuck.keller.sync;

import com.github.thorbenkuck.keller.datatypes.interfaces.QueuedAction;

public final class EmptySynchronize implements Synchronize {

	/**
	 * This method-call is ignored
	 */
	@Override
	public final void error() {
	}

	/**
	 * This Method-call is ignored
	 *
	 * @param queuedAction ignored
	 */
	@Override
	public void setOnError(QueuedAction queuedAction) {
	}

	/**
	 * This method-call is ignored
	 */
	@Override
	public final void goOn() {
	}

	/**
	 * This method-call is ignored
	 */
	@Override
	public final void reset() {
	}


	/**
	 * This method-call is ignored
	 */
	@Override
	public final void synchronize() {
	}
}
