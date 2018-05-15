package com.github.thorbenkuck.keller;

import com.github.thorbenkuck.keller.state.StateAction;
import com.github.thorbenkuck.keller.state.StateFollowup;
import com.github.thorbenkuck.keller.state.StateMachine;
import org.junit.Test;

public class StateTest {

	@Test
	public void test() {
		StateMachine stateMachine = new StateMachine();
		stateMachine.addDependency(new FirstDependency());

		stateMachine.start(new FirstState());
	}

	private class FirstDependency {
		int count = 0;
	}

	private class FirstState {
		@StateAction
		public void action() {
			System.out.println("In first state ..");
		}

		@StateFollowup
		public SecondState followup() {
			return new SecondState();
		}
	}

	private class SecondState {
		@StateAction
		public void action(FirstDependency dependency) {
			System.out.println("Fertig mit " + dependency.count);
		}
	}
}
