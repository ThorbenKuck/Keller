package com.github.thorbenkuck.keller.state;

import com.github.thorbenkuck.keller.state.exceptions.IllegalStateTransitionException;
import com.github.thorbenkuck.keller.state.exceptions.StateExecutionException;
import com.github.thorbenkuck.keller.state.transitions.StateTransition;
import com.github.thorbenkuck.keller.utility.Keller;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class InternalState extends Internal {

	private final Method actionMethod;

	InternalState(Method actionMethod, Method stateTransitionMethod, Method nextStateMethod, Method tearDownMethod, Object target) {
		super(stateTransitionMethod, nextStateMethod, tearDownMethod, target);
		this.actionMethod = actionMethod;
	}

	public void action(Object... params) {
		if(!hasActionMethod()) {
			return;
		}
		invoke(actionMethod, params);
	}

	public Method getActionMethod() {
		return actionMethod;
	}

	public boolean hasActionMethod() {
		return actionMethod != null;
	}

	@Override
	public void check() {
		if (willCreateStateTransition() && !StateTransition.class.isAssignableFrom(getStateTransitionMethod().getReturnType())) {
			throw new IllegalStateTransitionException(getStateTransitionMethod());
		}
	}
}
