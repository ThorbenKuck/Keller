package com.github.thorbenkuck.keller.state.transitions;

import com.github.thorbenkuck.keller.sync.Synchronize;

final class NativeFixedStateTransition implements StateTransition {

	private final Synchronize synchronize;

	public NativeFixedStateTransition(Synchronize synchronize) {
		this.synchronize = synchronize;
	}

	@Override
	public void finish() {
		synchronize.goOn();
	}

	@Override
	public void initialize() {

	}

	@Override
	public void reset() {
		synchronize.reset();
	}

	@Override
	public void transit() throws InterruptedException {
		synchronize.synchronize();
	}
}
