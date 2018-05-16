package com.github.thorbenkuck.keller.sync;

import com.github.thorbenkuck.keller.datatypes.interfaces.QueuedAction;

public interface Synchronize extends Awaiting {


	static Synchronize empty() {
		return SynchronizeCache.getEmpty();
	}

	static Synchronize createDefault() { return ofSemaphore(); }

	static Synchronize ofSemaphore() {
		return new NativeSemaphoreSynchronize();
	}

	static Synchronize ofCountDownLatch() {
		return new NativeCountDownLatchSynchronize();
	}

	static boolean isEmpty(Synchronize synchronize) {
		return isEmpty((Awaiting) synchronize);
	}

	static boolean isEmpty(Awaiting awaiting) {
		return SynchronizeCache.isEmpty(awaiting) || (awaiting != null && awaiting.getClass().equals(EmptySynchronize.class));
	}

	void error();

	void setOnError(QueuedAction queuedAction);

	void goOn();

	void reset();

}
