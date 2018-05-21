package com.github.thorbenkuck.keller.state.transitions;

final class EmptyStateTransition implements StateTransition {
	@Override
	public Object getFollowState() {
		return null;
	}

	@Override
	public void finish() {
	}

	@Override
	public void initialize() {
	}

	@Override
	public void reset() {
	}

	@Override
	public void transit() {

	}
}
