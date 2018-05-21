package com.github.thorbenkuck.keller.state;

import com.github.thorbenkuck.keller.state.transitions.StateTransition;
import com.github.thorbenkuck.keller.utility.Keller;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

abstract class Internal {

	private final Method stateTransitionMethod;
	private final Method nextStateMethod;
	private final Method tearDownMethod;
	protected final Object target;

	public Internal(Method stateTransitionMethod, Method nextStateMethod, Method tearDownMethod, Object target) {
		this.stateTransitionMethod = stateTransitionMethod;
		this.nextStateMethod = nextStateMethod;
		this.tearDownMethod = tearDownMethod;
		this.target = target;
	}

	protected Object invoke(Method method, Object... params) {
		boolean accessible = method.isAccessible();
		method.setAccessible(true);

		try {
			return method.invoke(target, params);
		} catch (final IllegalAccessException | InvocationTargetException e) {
			throw new IllegalStateException(e);
		} finally{
			method.setAccessible(accessible);
		}
	}

	public Object getNextState(Object... params) {
		if (nextStateMethod == null || Keller.isPrimitiveOrWrapperType(nextStateMethod.getReturnType())) {
			return null;
		}
		return invoke(nextStateMethod, params);
	}

	public StateTransition getStateTransition(Object... params) {
		if (stateTransitionMethod == null) {
			return StateTransition.dead();
		}
		Object stateTransition = invoke(stateTransitionMethod, params);
		if (stateTransition == null || !StateTransition.class.isAssignableFrom(stateTransition.getClass())) {
			return StateTransition.dead();
		}
		return (StateTransition) stateTransition;
	}

	public void dispatchTearDown(Object... params) {
		if (tearDownMethod != null) {
			invoke(tearDownMethod, params);
		}
	}

	public boolean stateTransitionRequiresParameters() {
		return willCreateStateTransition() && stateTransitionMethod.getParameterCount() > 0;
	}

	public boolean willCreateStateTransition() {
		return stateTransitionMethod != null;
	}

	public boolean willCreateNextState() {
		return nextStateMethod != null;
	}

	public boolean hasCustomTearDown() {
		return tearDownMethod != null;
	}

	public Method getStateTransitionMethod() {
		return stateTransitionMethod;
	}

	public Method getNextStateMethod() {
		return nextStateMethod;
	}

	public Method getTearDownMethod() {
		return tearDownMethod;
	}

	abstract void check();

	public Object getObject() {
		return target;
	}
}
