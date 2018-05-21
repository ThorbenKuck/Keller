package com.github.thorbenkuck.keller.state.transitions;

public interface StateTransitionFactory {

	static StateTransitionFactory access() {
		return new NativeStateTransitionFactory();
	}

}
