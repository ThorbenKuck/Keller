package com.github.thorbenkuck.keller.state;

import com.github.thorbenkuck.keller.state.exceptions.IllegalStateTransitionException;
import com.github.thorbenkuck.keller.state.transitions.StateTransition;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

final class InternalStateContext extends Internal {

	private final Method[] setStateMethods;
	private final Map<Class<?>, Method> argumentsToMethodMap = new HashMap<>();

	InternalStateContext(Method[] setStateMethods, Method stateTransitionMethod, Method nextStateMethod, Method tearDownMethod, Object target) {
		super(stateTransitionMethod, nextStateMethod, tearDownMethod, target);
		this.setStateMethods = setStateMethods;
	}

	private Method trySearch(Object nextState) {
		for(Class<?> type : argumentsToMethodMap.keySet()) {
			if(type.isAssignableFrom(nextState.getClass())) {
				return argumentsToMethodMap.get(type);
			}
		}
		return null;
	}

	public boolean setNextState(Object nextState) {
		Class<?> requiredMethodType = nextState.getClass();
		Method toExecute = argumentsToMethodMap.get(requiredMethodType);
		if (toExecute == null) {
			Method searched = trySearch(nextState);

			if(searched == null) {
				return false;
			} else {
				toExecute = searched;
			}
		}

		invoke(toExecute, nextState);
		return true;
	}

	@Override
	public void check() {
		if (setStateMethods == null) {
			throw new IllegalStateException("Could not locate Method annotated with @InjectState!\n" +
					"Such a Method is needed for the State Machine to communicate with the StateContext ");
		}

		for (final Method method : setStateMethods) {
			if (method.getParameterCount() != 1) {
				throw new IllegalStateException("Any Method, annotated with @InjectState should accept exactly one Object, the next State.\n"
						+ "Faulty Method: " + method + "\n"
						+ "Faulty Object");
			}
			argumentsToMethodMap.put(method.getParameters()[0].getType(), method);
		}

		if (willCreateStateTransition() && !StateTransition.class.isAssignableFrom(getStateTransitionMethod().getReturnType())) {
			throw new IllegalStateTransitionException(getStateTransitionMethod());
		}
	}

}
