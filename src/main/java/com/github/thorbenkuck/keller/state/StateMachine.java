package com.github.thorbenkuck.keller.state;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class StateMachine {

	private final Value<Object> currentState = Value.emptySynchronized();
	private final Map<Class<?>, Object> dependencies = new HashMap<>();

	public void start(Object object) {
		currentState.set(object);
		while(!currentState.isEmpty()) {
			run();
		}
	}

	public void addDependency(Object object) {
		dependencies.put(object.getClass(), object);
	}

	private void run() {
		Object current = currentState.get();
		dispatchAction(current);
		dispatchFollowup(current);
	}

	private void dispatchFollowup(Object current) {
		Method actionMethod = getFollowupStateMethod(current.getClass());
		if(actionMethod == null) {
			tryApplyNewState(null);
			return;
		}

		boolean accessible = actionMethod.isAccessible();
		actionMethod.setAccessible(true);

		try {
			tryApplyNewState(actionMethod.invoke(current, constructParameters(actionMethod)));
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new IllegalStateException(e);
		} finally {
			actionMethod.setAccessible(accessible);
		}
	}

	private void dispatchAction(Object current) {
		Method actionMethod = getActionMethod(current.getClass());
		if(actionMethod == null) {
			throw new IllegalStateException("Could not locate Method annotated with StateAction");
		}

		boolean accessible = actionMethod.isAccessible();
		actionMethod.setAccessible(true);

		try {
			actionMethod.invoke(current, constructParameters(actionMethod));
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new IllegalStateException(e);
		} finally {
			actionMethod.setAccessible(accessible);
		}
	}

	private Method getActionMethod(Class<?> clazz) {
		for(Method method : clazz.getMethods()) {
			if(method.isAnnotationPresent(StateAction.class)) {
				return method;
			}
		}
		return null;
	}

	private Method getFollowupStateMethod(Class<?> clazz) {
		for(Method method : clazz.getMethods()) {
			if(method.isAnnotationPresent(StateFollowup.class)) {
				return method;
			}
		}
		return null;
	}

	private Object[] constructParameters(Method method) {
		final Object[] parameters = new Object[method.getParameterCount()];
		final Class<?>[] parameterTypes = method.getParameterTypes();

		for(int i = 0 ; i < method.getParameterCount() ; i++) {
			Class<?> currentType = parameterTypes[i];
			Object object = dependencies.get(currentType);
			parameters[i] = object;
		}

		return parameters;
	}

	private void tryApplyNewState(Object object) {
		if(object == null) {
			currentState.clear();
		} else {
			currentState.set(object);
		}
	}
}
