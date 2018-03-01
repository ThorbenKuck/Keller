package com.github.thorbenkuck.keller.sync;

public interface Synchronize extends Awaiting {

	Synchronize EMPTY_SYNCHRONIZE = new EmptySynchronize();

	static Synchronize empty() {
		return EMPTY_SYNCHRONIZE;
	}

	static Synchronize createDefault() { return new DefaultSynchronize(); }

	void error();

	void goOn();

	void reset();

}
