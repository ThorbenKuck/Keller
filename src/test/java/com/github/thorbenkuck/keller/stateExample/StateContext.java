package com.github.thorbenkuck.keller.stateExample;

import com.github.thorbenkuck.keller.state.annotations.InjectState;

public class StateContext {

	private Statelike myState;

	@InjectState
	public void setState(final Statelike newState) {
		myState = newState;
	}

	public void writeName(final String name) {
		myState.writeName(name);
	}

}
