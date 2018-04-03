package com.github.thorbenkuck.keller.sync;

public interface Synchronize extends Awaiting {


	static Synchronize empty() {
		return SynchronizeCache.getEmpty();
	}

	static Synchronize createDefault() { return ofSemaphore(); }

	static Synchronize ofSemaphore() {
		return new DefaultSemaphoreSynchronize();
	}

	static Synchronize ofCountDownLatch() {
		return new DefaultCountDownLatchSynchronize();
	}

	static boolean isEmpty(Synchronize synchronize) {
		return isEmpty((Awaiting) synchronize);
	}

	static boolean isEmpty(Awaiting awaiting) {
		return SynchronizeCache.isEmpty(awaiting) || (awaiting != null && awaiting.getClass().equals(EmptySynchronize.class));
	}

	void error();

	void goOn();

	void reset();

}
