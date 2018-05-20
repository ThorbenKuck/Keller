package com.github.thorbenkuck.keller.state;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.keller.di.DependencyManager;
import com.github.thorbenkuck.keller.state.annotations.NextState;
import com.github.thorbenkuck.keller.state.annotations.StateAction;
import com.github.thorbenkuck.keller.state.annotations.StateTransitionFactory;
import com.github.thorbenkuck.keller.state.annotations.TearDown;
import com.github.thorbenkuck.keller.state.transitions.StateTransition;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

final class NativeStateMachine implements StateMachine {

	private final Value<InternalState> currentStateValue = Value.emptySynchronized();
	private final Value<StateTransition> currentStateTransitionValue = Value.emptySynchronized();
	private final Value<DependencyManager> dependencyManagerValue = Value.synchronize(DependencyManager.create());
	private static final Object[] EMPTY_ARGS = new Object[0];
	private final Value<Boolean> running = Value.synchronize(false);

	private InternalState constructInternalState(Object object) {
		Method actionMethod = getActionMethod(object.getClass());
		Method nextStateMethod = getFollowupStateMethod(object.getClass());
		Method stateTransitionMethod = getStateTransitionFactory(object.getClass());
		Method tearDownMethod = getTearDownMethod(object.getClass());
		InternalState state = new InternalState(actionMethod, stateTransitionMethod, nextStateMethod, tearDownMethod, object);

		state.check();
		return state;
	}

	private void handleCurrentState() {
		InternalState current = currentStateValue.get();
		dispatchStateTransition(current);
		dispatchAction(current);
		dispatchNextState(current);
		dispatchTearDown(current);
	}

	private void dispatchStateTransition(InternalState current) {
		Method stateTransitionMethod = current.getStateTransitionMethod();
		Object[] params = constructParameters(stateTransitionMethod);
		StateTransition stateTransition = current.getStateTransition(params);

		tryApplyNewStateTransition(stateTransition);
	}

	private void dispatchTearDown(InternalState current) {
		Method tearDownMethod = current.getTearDownMethod();
		current.dispatchTearDown(constructParameters(tearDownMethod));
	}

	private void dispatchNextState(InternalState current) {
		StateTransition stateTransition = currentStateTransitionValue.get();
		try {
			stateTransition.transit();
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}

		Method nextStateMethod = current.getNextStateMethod();
		tryApplyNewState(current.getNextState(constructParameters(nextStateMethod)));
	}

	private void dispatchAction(InternalState internalState) {
		StateTransition stateTransition = currentStateTransitionValue.get();
		stateTransition.initialize();

		Method actionMethod = internalState.getActionMethod();
		internalState.action(constructParameters(actionMethod));
	}

	private <T extends Annotation> Method findMethodWithAnnotation(Class<?> clazz, Class<? extends T> annotation) {
		for (Method method : clazz.getMethods()) {
			if (method.isAnnotationPresent(annotation)) {
				return method;
			}
		}
		return null;
	}

	private Method getActionMethod(Class<?> clazz) {
		return findMethodWithAnnotation(clazz, StateAction.class);
	}

	private Method getFollowupStateMethod(Class<?> clazz) {
		return findMethodWithAnnotation(clazz, NextState.class);
	}

	private Method getStateTransitionFactory(Class<?> clazz) {
		return findMethodWithAnnotation(clazz, StateTransitionFactory.class);
	}

	private Method getTearDownMethod(Class<?> clazz) {
		return findMethodWithAnnotation(clazz, TearDown.class);
	}

	private Object[] constructParameters(Method method) {
		if (method == null) {
			return EMPTY_ARGS;
		}
		if (method.getParameterCount() == 0) {
			return EMPTY_ARGS;
		}
		final Object[] parameters = new Object[method.getParameterCount()];
		final Class<?>[] parameterTypes = method.getParameterTypes();
		Annotation[][] annotations = method.getParameterAnnotations();

		for (int i = 0; i < method.getParameterCount(); i++) {
			Annotation[] current = annotations[i];
			Class<?> currentType = parameterTypes[i];
			Object object = dependencyManagerValue.get().getAccordingToAnnotation(currentType, current);
			parameters[i] = object;
		}

		return parameters;
	}

	private void tryApplyNewState(Object object) {
		if (object == null) {
			currentStateValue.clear();
		} else {
			currentStateValue.set(constructInternalState(object));
		}
	}

	private void tryApplyNewStateTransition(StateTransition nextStateTransition) {
		currentStateTransitionValue.set(nextStateTransition);
	}

	@Override
	public void stop() {
		running.set(false);
	}

	@Override
	public void start(Object object) {
		currentStateValue.set(constructInternalState(object));
		running.set(true);
		while (!currentStateValue.isEmpty() && running.get()) {
			handleCurrentState();
		}
	}

	@Override
	public void addStateDependency(Object object) {
		dependencyManagerValue.get().inject(object);
	}

	@Override
	public void setDependencyManager(DependencyManager dependencyManager) {
		dependencyManagerValue.set(dependencyManager);
	}

	@Override
	public void step() {
		currentStateTransitionValue.get().finish();
	}
}
