package com.github.thorbenkuck.keller.state.transitions;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.keller.sync.Synchronize;

class NativeStateTransition implements StateTransition {

	private final Value<Synchronize> synchronizeValue = Value.emptySynchronized();
	private final Value<Object> followStateValue = Value.empty();

	NativeStateTransition() {}

	NativeStateTransition(Object followState) {
		followStateValue.set(followState);
	}

	@Override
	public Object getFollowState() {
		return followStateValue.get();
	}

	@Override
	public void finish() {
		if (!synchronizeValue.isEmpty()) {
			synchronizeValue.get().goOn();
		}
	}

	@Override
	public void initialize() {
		if (synchronizeValue.isEmpty()) {
			synchronizeValue.set(Synchronize.createDefault());
		}
	}

	@Override
	public void reset() {
		synchronizeValue.clear();
	}

	@Override
	public void transit() throws InterruptedException {
		synchronizeValue.get().synchronize();
	}
}
