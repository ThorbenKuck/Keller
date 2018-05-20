package com.github.thorbenkuck.keller;

import com.github.thorbenkuck.keller.di.annotations.*;
import com.github.thorbenkuck.keller.state.*;
import com.github.thorbenkuck.keller.state.annotations.NextState;
import com.github.thorbenkuck.keller.state.annotations.StateAction;
import com.github.thorbenkuck.keller.state.annotations.StateTransitionFactory;
import com.github.thorbenkuck.keller.state.annotations.TearDown;
import com.github.thorbenkuck.keller.state.transitions.StateTransition;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class StateTest {

	@Test
	public void test() {
		StateMachine stateMachine = StateMachine.create();
		stateMachine.addStateDependency(new SecondDependency());
		stateMachine.addStateDependency(new Counter());
		stateMachine.start(new FirstState());
	}

	@Implementation(SecondDependency.class)
	private interface SomeInterface {
	}

	@Cache
	private @Bind class SecondDependency implements @Bind SomeInterface {
		@Use
		public SecondDependency() {
			System.out.println("Instantiated SecondDependency");
		}
	}

	@Cache
	private class FirstDependency {
		int count;

		@Use
		public FirstDependency(SomeInterface secondDependency) {
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
		public Object nextState() {
			return new CountingState();
		}

		@TearDown
		public void tearDown() {
			System.out.println("First State destructed");
		}
	}

	private class CountingState {

		private Counter counter;

		@StateAction
		public void action(Counter counter) {
			counter.increase();
			System.out.println("Count at " + counter.getCount());
			this.counter = counter;
		}

		@NextState
		public Object nextState() {
			if(counter.getCount() >= 3) {
				return new SecondState();
			} else {
				return this;
			}
		}
	}

	private class SecondState {
		@StateAction
		public void action(FirstDependency dependency) {
			System.out.println("In Second State " + dependency.count);
		}

		@StateTransitionFactory
		public CustomStateTransition transition() {
			return new CustomStateTransition();
		}

		@NextState
		public EndState nexState() {
			return EndState.get();
		}
	}

	private class CustomStateTransition implements StateTransition {

		final StateTransition base = StateTransition.dead();

		@Override
		public void finish() {
			base.finish();
		}

		@Override
		public void initialize() {
			base.initialize();
		}

		@Override
		public void reset() {
			base.reset();
		}

		@Override
		public void transit() throws InterruptedException {
			base.transit();
		}
	}

	private class Counter {

		private int count;

		public Counter() {
			count = 0;
		}

		public int getCount() {
			return count;
		}

		public void increase() {
			++count;
		}
	}
}
