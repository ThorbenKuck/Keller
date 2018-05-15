package com.github.thorbenkuck.keller.state;

public interface State {

	static State empty() {
		return StateCache.EMPTY_STATE;
	}

	static boolean isEmpty(State state) {
		return state == StateCache.EMPTY_STATE;
	}

	void action();

	default State next() {
		return StateCache.EMPTY_STATE;
	}

}
