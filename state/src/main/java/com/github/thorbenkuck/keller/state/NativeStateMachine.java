package com.github.thorbenkuck.keller.state;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.keller.di.DependencyManager;
import com.github.thorbenkuck.keller.state.transitions.StateTransition;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

final class NativeStateMachine implements StateMachine {

	private final Value<InternalState> currentStateValue = Value.emptySynchronized();
	private final Value<StateTransition> currentStateTransitionValue = Value.emptySynchronized();
	private final Value<DependencyManager> dependencyManagerValue = Value.synchronize(DependencyManager.create());

	@Override
	public void start(Object object) {
		currentStateValue.set(constructInternalState(object));
		while(!currentStateValue.isEmpty()) {
			handleCurrentState();
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

	@Override
	public void continueToNextState() {
		currentStateTransitionValue.get().finish();
	}

	private InternalState constructInternalState(Object object) {
		Method actionMethod = getActionMethod(object.getClass());
		if(actionMethod == null) {
			throw new IllegalStateException("Could not locate Method annotated with StateAction");
		}

		Method nextStateMethod = getFollowupStateMethod(object.getClass());
		Method stateTransitionMethod = getStateTransitionFactory(object.getClass());
		InternalState state = new InternalState(actionMethod, stateTransitionMethod, nextStateMethod, object);

		state.check();
		return state;
	}

	private void handleCurrentState() {
		InternalState current = currentStateValue.get();
		dispatchStateTransition(current);
		dispatchAction(current);
		dispatchNextState(current);
	}

	private void dispatchStateTransition(InternalState current) {
		StateTransition stateTransition;

		if (!current.willCreateStateTransition() || !current.stateTransitionRequiresParameters()) {
			stateTransition = current.getStateTransition();
		} else {
			Method stateTransitionMethod = current.getStateTransitionMethod();
			Object[] params = constructParameters(stateTransitionMethod);
			stateTransition = current.getStateTransition(params);
		}

		tryApplyNewStateTransition(stateTransition);
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
		for(Method method : clazz.getMethods()) {
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

	private Object[] constructParameters(Method method) {
		final Object[] parameters = new Object[method.getParameterCount()];
		final Class<?>[] parameterTypes = method.getParameterTypes();
		Annotation[][] annotations = method.getParameterAnnotations();

		for(int i = 0 ; i < method.getParameterCount() ; i++) {
			Annotation[] current = annotations[i];
			Class<?> currentType = parameterTypes[i];
			Object object = dependencyManagerValue.get().getAccordingToAnnotation(currentType, current);
			parameters[i] = object;
		}

		return parameters;
	}

	private void tryApplyNewState(Object object) {
		if(object == null) {
			currentStateValue.clear();
		} else {
			currentStateValue.set(constructInternalState(object));
		}
	}

	private void tryApplyNewStateTransition(StateTransition nextStateTransition) {
		currentStateTransitionValue.set(nextStateTransition);
	}
}
