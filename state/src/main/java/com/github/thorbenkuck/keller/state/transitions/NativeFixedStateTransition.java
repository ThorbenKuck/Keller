package com.github.thorbenkuck.keller.state.transitions;

import com.github.thorbenkuck.keller.sync.Synchronize;

final class NativeFixedStateTransition implements StateTransition {

	private final Synchronize synchronize;
	private final Object followingState;

	public NativeFixedStateTransition(Synchronize synchronize, Object followingState) {
		this.synchronize = synchronize;
		this.followingState = followingState;
	}

	@Override
	public Object getFollowState() {
		return followingState;
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
