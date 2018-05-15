package com.github.thorbenkuck.keller.state;

class StateCache {

	static final State EMPTY_STATE = new EmptyState();

	static State accessEmpty() {
		return EMPTY_STATE;
	}

	private static final class EmptyState implements State {

		@Override
		public void action() {

		}

		@Override
		public State next() {
			return null;
		}
	}
}
