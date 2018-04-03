package com.github.thorbenkuck.keller.sync;

public class DefaultCountDownLatchSynchronize extends AbstractCountDownLatchSynchronize {

	private Runnable onError = () -> System.out.println(this + " encountered an error!");

	public DefaultCountDownLatchSynchronize() {
		this(1);
	}

	public DefaultCountDownLatchSynchronize(final int numberOfActions) {
		super(numberOfActions);
	}

	/**
	 * This Method will call the Runnable, set via {@link #setOnError(Runnable)}.
	 * {@inheritDoc}
	 */
	@Override
	public void error() {
		synchronized (this) {
			onError.run();
		}
	}

	/**
	 * Sets an Runnable, that should be executed if an error occurred
	 *
	 * @param runnable the runnable, that should be executed.
	 */
	public void setOnError(Runnable runnable) {
		synchronized (this) {
			onError = runnable;
		}
	}
}
