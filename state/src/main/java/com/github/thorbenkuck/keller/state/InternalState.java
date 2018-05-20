package com.github.thorbenkuck.keller.state;

import com.github.thorbenkuck.keller.state.transitions.StateTransition;
import com.github.thorbenkuck.keller.utility.Keller;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class InternalState {

	private final Method actionMethod;
	private final Method stateTransitionMethod;
	private final Method nextStateMethod;
	private final Method tearDownMethod;
	private final Object target;

	InternalState(Method actionMethod, Method stateTransitionMethod, Method nextStateMethod, Method tearDownMethod, Object target) {
		this.actionMethod = actionMethod;
		this.stateTransitionMethod = stateTransitionMethod;
		this.nextStateMethod = nextStateMethod;
		this.tearDownMethod = tearDownMethod;
		this.target = target;
	}

	private Object invoke(Method method, Object target, Object... params) {
		boolean accessible = method.isAccessible();
		method.setAccessible(true);

		try {
			return method.invoke(target, params);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new IllegalStateException(e);
		} finally {
			method.setAccessible(accessible);
		}
	}

	public void action(Object... params) {
		invoke(actionMethod, target, params);
	}

	public Object getNextState(Object... params) {
		if (nextStateMethod == null || Keller.isPrimitiveOrWrapperType(nextStateMethod.getReturnType())) {
			return null;
		}
		return invoke(nextStateMethod, target, params);
	}

	public StateTransition getStateTransition(Object... params) {
		if (stateTransitionMethod == null) {
			return StateTransition.dead();
		}
		Object stateTransition = invoke(stateTransitionMethod, target, params);
		if (stateTransition == null || !StateTransition.class.isAssignableFrom(stateTransition.getClass())) {
			return StateTransition.dead();
		}
		return (StateTransition) stateTransition;
	}

	public void dispatchTearDown(Object... params) {
		if (tearDownMethod != null) {
			invoke(tearDownMethod, target, params);
		}
	}

	public boolean stateTransitionRequiresParameters() {
		return willCreateStateTransition() && stateTransitionMethod.getParameterCount() > 0;
	}

	public boolean willCreateStateTransition() {
		return stateTransitionMethod != null;
	}

	public boolean hasCustomTearDown() {
		return tearDownMethod != null;
	}

	public Method getActionMethod() {
		return actionMethod;
	}

	public Method getStateTransitionMethod() {
		return stateTransitionMethod;
	}

	public Method getNextStateMethod() {
		return nextStateMethod;
	}

	public void check() {
		if (actionMethod == null) {
			throw new IllegalStateException("No method to Execute State provided!");
		}

		if (willCreateStateTransition() && !StateTransition.class.isAssignableFrom(stateTransitionMethod.getReturnType())) {
			throw new IllegalStateException("Methods annotated with @StateTransitionFactory have to construct a StateTransition or any custom implementation!\n"
					+ "Provided: " + stateTransitionMethod.getReturnType() + "\n"
					+ "Expected: " + StateTransition.class + " (or any implementation of such)");
		}
	}

	public Method getTearDownMethod() {
		return tearDownMethod;
	}
}
