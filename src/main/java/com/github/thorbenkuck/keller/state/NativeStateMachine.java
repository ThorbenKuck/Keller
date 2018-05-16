package com.github.thorbenkuck.keller.state;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.keller.di.DependencyManager;
import com.github.thorbenkuck.keller.utility.Keller;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class NativeStateMachine implements StateMachine {

	private final Value<Object> currentStateValue = Value.emptySynchronized();
	private final Value<DependencyManager> dependencyManagerValue = Value.synchronize(DependencyManager.create());

	@Override
	public void start(Object object) {
		currentStateValue.set(object);
		while(!currentStateValue.isEmpty()) {
			run();
		}
	}

	@Override
	public void addDependency(Object object) {
		dependencyManagerValue.get().addPreConstructedDependency(object);
	}

	@Override
	public void setDependencyManager(DependencyManager dependencyManager) {
		dependencyManagerValue.set(dependencyManager);
	}

	private void run() {
		Object current = currentStateValue.get();
		dispatchAction(current);
		dispatchFollowup(current);
	}

	private void dispatchFollowup(Object current) {
		Method actionMethod = getFollowupStateMethod(current.getClass());
		if(actionMethod == null || Keller.isPrimitiveOrWrapperType(actionMethod.getReturnType())) {
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
			if(method.isAnnotationPresent(NextState.class)) {
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
			Object object = dependencyManagerValue.get().get(currentType);
			parameters[i] = object;
		}

		return parameters;
	}

	private void tryApplyNewState(Object object) {
		if(object == null) {
			currentStateValue.clear();
		} else {
			currentStateValue.set(object);
		}
	}
}
