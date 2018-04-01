package com.github.thorbenkuck.keller.sync;

public interface Synchronize extends Awaiting {


	static Synchronize empty() {
		return SynchronizeCache.getEmpty();
	}

	static Synchronize createDefault() { return new DefaultSynchronize(); }

	static boolean isEmpty(Synchronize synchronize) {
		return synchronize == empty();
	}

	static boolean isEmpty(Awaiting awaiting) {
		return awaiting == empty();
	}

	void error();

	void goOn();

	void reset();

}
