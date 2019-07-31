package com.github.thorbenkuck.keller.sync;

import com.github.thorbenkuck.keller.annotations.Asynchronous;

import java.util.concurrent.Semaphore;

public abstract class AbstractSemaphoreSynchronize implements Synchronize {

	protected final Semaphore mutex;
	protected static final int INITIAL_PERMITS = 0;

	protected AbstractSemaphoreSynchronize() {
		this.mutex = new Semaphore(INITIAL_PERMITS);
	}

	@Asynchronous
	@Override
	public void synchronize() throws InterruptedException {
		mutex.acquire();
		mutex.release();
	}

	@Asynchronous
	@Override
	public void goOn() {
		mutex.release();
	}

	@Asynchronous
	@Override
	public void reset() {
		while(mutex.availablePermits() != INITIAL_PERMITS) {
			mutex.release();
		}
	}
}
