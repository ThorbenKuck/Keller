package com.github.thorbenkuck.keller.state.exceptions;

import com.github.thorbenkuck.keller.state.transitions.StateTransition;

import java.lang.reflect.Method;

public class IllegalStateTransitionException extends IllegalStateException {

	public IllegalStateTransitionException(Method stateTransitionMethod) {
		super("Methods annotated with @StateTransitionFactory have to construct a StateTransition or any subtype!\n"
				+ "Provided: " + stateTransitionMethod.getReturnType() + "\n"
				+ "Expected: " + StateTransition.class + " (or any implementation of such)");
	}

}
