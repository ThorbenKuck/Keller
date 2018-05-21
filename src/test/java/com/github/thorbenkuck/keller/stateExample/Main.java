package com.github.thorbenkuck.keller.stateExample;

import com.github.thorbenkuck.keller.state.StateMachine;

public class Main {

	public static void main(String[] args) {
		final StateContext stateContext = new StateContext();
		final StateMachine stateMachine = StateMachine.create();
		stateMachine.setStateContext(stateContext);
		stateMachine.parallel(new StateA());
		handle(stateContext);
		stateMachine.stop();
	}

	public static void handle(StateContext stateContext) {
		stateContext.writeName("Montag");
		stateContext.writeName("Dienstag");
		stateContext.writeName("Mittwoch");
		stateContext.writeName("Donnerstag");
		stateContext.writeName("Freitag");
		stateContext.writeName("Samstag");
		stateContext.writeName("Sonntag");
	}

}
