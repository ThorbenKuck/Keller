package com.github.thorbenkuck.keller.stateExample;

import com.github.thorbenkuck.keller.state.annotations.NextState;
import com.github.thorbenkuck.keller.state.annotations.StateAction;
import com.github.thorbenkuck.keller.state.annotations.StateTransitionFactory;
import com.github.thorbenkuck.keller.state.annotations.TearDown;
import com.github.thorbenkuck.keller.state.transitions.StateTransition;
import com.github.thorbenkuck.keller.sync.Synchronize;

public class StateB implements Statelike {
	/**
	 * State counter
	 */
	private int count = 0;
	private final StateTransition stateTransition = StateTransition.open();
	private final Synchronize synchronize = Synchronize.ofCountDownLatch();

	@Override
	public void writeName(String name) {
		System.out.println(name.toUpperCase());
		if(++count > 1) {
			stateTransition.finish();
			try {
				synchronize.synchronize();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@StateTransitionFactory
	public StateTransition getStateTransition() {
		return stateTransition;
	}

	@NextState
	public Statelike nextState() {
		return new StateA();
	}

	@TearDown
	public void tearDown() {
		synchronize.goOn();
	}
}
