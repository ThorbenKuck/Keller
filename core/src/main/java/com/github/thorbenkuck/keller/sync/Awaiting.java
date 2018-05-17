package com.github.thorbenkuck.keller.sync;

public interface Awaiting {

	/**
	 * This method blocks the current Thread, until the parallel Operation is finished.
	 *
	 * @throws InterruptedException if the blocking-mechanism is interrupted in some form or another
	 */
	void synchronize() throws InterruptedException;

}
