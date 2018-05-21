package com.github.thorbenkuck.keller.state.exceptions;

import java.lang.reflect.Method;

public class StateExecutionException extends RuntimeException {

	public StateExecutionException(Method method) {
		super("Error executing " + method);
	}

	public StateExecutionException(String message, Method method) {
		super(message + "\nFaulty Method: " + method);
	}

	public StateExecutionException(String message, Method method, Throwable e) {
		super(message + "\nFaulty Method: " + method, e);
	}

}
