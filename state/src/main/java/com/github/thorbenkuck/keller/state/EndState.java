package com.github.thorbenkuck.keller.state;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.keller.state.annotations.NextState;
import com.github.thorbenkuck.keller.state.annotations.StateAction;

public final class EndState {

	private static Value<EndState> endStateValue = Value.emptySynchronized();

	public static EndState get() {
		if(endStateValue.isEmpty()) {
			endStateValue.set(new EndState());
		}

		return endStateValue.get();
	}

	private EndState() {
	}

	@StateAction
	public void action() {}

	@NextState
	public Void nexState() {
		return null;
	}

}
