package com.github.thorbenkuck.keller;

import com.github.thorbenkuck.keller.di.SingleInstanceOnly;
import com.github.thorbenkuck.keller.state.*;
import com.github.thorbenkuck.keller.state.transitions.StateTransition;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class StateTest {

	@Test
	public void test() {
		StateMachine stateMachine = StateMachine.create();
		stateMachine.addDependency(new SecondDependency());
		stateMachine.start(new FirstState());
	}

	@SingleInstanceOnly
	private class SecondDependency {
		public SecondDependency() {
			System.out.println("Instantiated SecondDependency");
		}
	}

	private class FirstDependency {
		int count;

		public FirstDependency(SecondDependency secondDependency) {
			count = 0;
			System.out.println("Instantiated FirstDependency");
		}
	}

	private class FirstState {

		@StateAction
		public void action(SecondDependency secondDependency) {
			System.out.println("In first state ..");
		}

		@StateTransitionFactory
		public StateTransition construct() {
			return StateTransition.openAsTimer(2, TimeUnit.SECONDS);
		}

		@NextState
		public SecondState followup() {
			return new SecondState();
		}
	}

	private class SecondState {
		@StateAction
		public void action(FirstDependency dependency) {
			System.out.println("In Second State " + dependency.count);
		}

		@NextState
		public EndState nexState() {
			return EndState.get();
		}
	}
}
