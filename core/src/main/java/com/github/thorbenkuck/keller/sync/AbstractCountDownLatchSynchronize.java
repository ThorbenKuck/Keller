package com.github.thorbenkuck.keller.sync;

import com.github.thorbenkuck.keller.annotations.Asynchronous;

import java.util.concurrent.CountDownLatch;

public abstract class AbstractCountDownLatchSynchronize implements Synchronize {

	protected final int numberOfActions;
	protected CountDownLatch countDownLatch;

	protected AbstractCountDownLatchSynchronize() {
		this(1);
	}

	protected AbstractCountDownLatchSynchronize(final int numberOfActions) {
		if (numberOfActions < 1) {
			throw new IllegalArgumentException("Number of actions cannot be smaller than 1!");
		}
		this.numberOfActions = numberOfActions;
		this.countDownLatch = new CountDownLatch(numberOfActions);
	}

	@Asynchronous
	@Override
	public void synchronize() throws InterruptedException {
		if(getCount() != 0) {
			countDownLatch.await();
		}
	}

	@Asynchronous
	@Override
	public void goOn() {
		synchronized (this) {
			countDownLatch.countDown();
		}
	}

	@Asynchronous
	@Override
	public void reset() {
		long count = getCount();
		while (count > 0) {
			goOn();
			synchronized(this) {
				count = getCount();
			}
		}

		synchronized (this) {
			countDownLatch = new CountDownLatch(numberOfActions);
		}
	}

	protected final long getCount() {
		long count;
		synchronized(this) {
			if(countDownLatch == null) {
				count = 0;
			} else {
				count = countDownLatch.getCount();
			}
		}

		return count;
	}
}
