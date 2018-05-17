package com.github.thorbenkuck.keller.state.transitions;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.keller.sync.Synchronize;

public class NativeStateTransition implements StateTransition {

	private final Value<Synchronize> synchronizeValue = Value.emptySynchronized();

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
