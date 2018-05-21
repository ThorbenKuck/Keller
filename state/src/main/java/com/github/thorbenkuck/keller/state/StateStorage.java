package com.github.thorbenkuck.keller.state;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.keller.di.DependencyManager;
import com.github.thorbenkuck.keller.state.annotations.*;
import com.github.thorbenkuck.keller.state.transitions.StateTransition;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

class StateStorage {

	private final Value<DependencyManager> dependencyManagerValue = Value.synchronize(DependencyManager.create());
	private final Value<InternalState> currentStateValue = Value.emptySynchronized();
	private final Value<StateTransition> currentStateTransitionValue = Value.emptySynchronized();
	private final Value<InternalStateContext> stateContextValue = Value.emptySynchronized();
	private static final Object[] EMPTY_ARGS = new Object[0];

	private InternalStateContext constructStateContext(final Object object) {
		final Method[] contextInject = findAllMethodsWithAnnotation(object.getClass(), InjectState.class);
		final Method nextStateMethod = getNextStateMethod(object.getClass());
		final Method stateTransitionMethod = getStateTransitionFactory(object.getClass());
		final Method tearDownMethod = getTearDownMethod(object.getClass());
		final InternalStateContext context = new InternalStateContext(contextInject, stateTransitionMethod, nextStateMethod, tearDownMethod, object);

		context.check();
		return context;
	}

	private <T extends Annotation> Method[] findAllMethodsWithAnnotation(final Class<?> clazz, final Class<? extends T> annotation) {
		final List<Method> resultList = new ArrayList<>();

		for(final Method method : clazz.getMethods()) {
			if(method.isAnnotationPresent(annotation)) {
				resultList.add(method);
			}
		}

		return resultList.toArray(new Method[resultList.size()]);
	}

	private <T extends Annotation> Method findMethodWithAnnotation(final Class<?> clazz, final Class<? extends T> annotation) {
		for (final Method method : clazz.getMethods()) {
			if (method.isAnnotationPresent(annotation)) {
				return method;
			}
		}
		return null;
	}

	private Method getActionMethod(final Class<?> clazz) {
		return findMethodWithAnnotation(clazz, StateAction.class);
	}

	private Method getNextStateMethod(final Class<?> clazz) {
		return findMethodWithAnnotation(clazz, NextState.class);
	}

	private Method getStateTransitionFactory(final Class<?> clazz) {
		return findMethodWithAnnotation(clazz, StateTransitionFactory.class);
	}

	private Method getTearDownMethod(final Class<?> clazz) {
		return findMethodWithAnnotation(clazz, TearDown.class);
	}


	private InternalState constructInternalState(final Object object) {
		final Method actionMethod = getActionMethod(object.getClass());
		final Method nextStateMethod = getNextStateMethod(object.getClass());
		final Method stateTransitionMethod = getStateTransitionFactory(object.getClass());
		final Method tearDownMethod = getTearDownMethod(object.getClass());
		final InternalState state = new InternalState(actionMethod, stateTransitionMethod, nextStateMethod, tearDownMethod, object);

		state.check();
		return state;
	}

	private void dispatchStateInject(InternalStateContext context) {
		if (currentStateValue.isEmpty()) {
			return;
		}
		final InternalState current = currentStateValue.get();
		context.setNextState(current.getObject());
	}

	private Object[] constructParameters(final Method method) {
		if (method == null) {
			return EMPTY_ARGS;
		}
		if (method.getParameterCount() == 0) {
			return EMPTY_ARGS;
		}
		final Object[] parameters = new Object[method.getParameterCount()];
		final Class<?>[] parameterTypes = method.getParameterTypes();
		final Annotation[][] annotations = method.getParameterAnnotations();

		for (int i = 0; i < method.getParameterCount(); i++) {
			final Annotation[] current = annotations[i];
			final Class<?> currentType = parameterTypes[i];
			final Object object = dependencyManagerValue.get().getAccordingToAnnotation(currentType, current);
			parameters[i] = object;
		}

		return parameters;
	}

	private void tryApplyNewState(final Object object) {
		if (object == null) {
			currentStateValue.clear();
		} else {
			currentStateValue.set(constructInternalState(object));
		}
	}

	private void tryApplyNewStateTransition(final StateTransition nextStateTransition) {
		currentStateTransitionValue.set(nextStateTransition);
	}

	private void dispatchStateTransition(final Internal current) {
		if(current == null) {
			tryApplyNewStateTransition(StateTransition.dead());
			return;
		}
		final Method stateTransitionMethod = current.getStateTransitionMethod();
		final Object[] params = constructParameters(stateTransitionMethod);
		final StateTransition stateTransition = current.getStateTransition(params);

		tryApplyNewStateTransition(stateTransition);
	}

	private void dispatchTearDown(final Internal current) {
		final Method tearDownMethod = current.getTearDownMethod();
		final Object[] args = constructParameters(tearDownMethod);

		current.dispatchTearDown(args);
	}

	private void dispatchNextState(final Internal current) {
		final StateTransition stateTransition = currentStateTransitionValue.get();

		try {
			stateTransition.transit();
		} catch (final InterruptedException e) {
			throw new IllegalStateException(e);
		}

		Object nextState = stateTransition.getFollowState();
		if(nextState != null) {
			tryApplyNewState(nextState);
		} else {
			final Method nextStateMethod = current.getNextStateMethod();
			final Object[] args = constructParameters(nextStateMethod);
			nextState = current.getNextState(args);
			tryApplyNewState(nextState);
		}
	}

	private void dispatchAction(final InternalState internalState) {
		final StateTransition stateTransition = currentStateTransitionValue.get();

		stateTransition.initialize();

		if(internalState.hasActionMethod()) {
			final Method actionMethod = internalState.getActionMethod();
			final Object[] args = constructParameters(actionMethod);

			internalState.action(args);
		}
	}

	void handleCurrentState() {
		final InternalState current = currentStateValue.get();
		final InternalStateContext context = stateContextValue.get();

		if(current.willCreateStateTransition()) {
			dispatchStateTransition(current);
		} else {
			dispatchStateTransition(context);
		}

		dispatchAction(current);

		if(context != null && context.willCreateNextState()) {
			dispatchNextState(context);
		} else {
			dispatchNextState(current);
		}

		if(context != null) {
			dispatchTearDown(context);
			dispatchStateInject(context);
		}

		dispatchTearDown(current);
	}

	void setCurrentState(Object object) {
		currentStateValue.set(constructInternalState(object));
	}

	void setStateContext(Object object) {
		stateContextValue.set(constructStateContext(object));
	}

	boolean hasNext() {
		return currentStateValue.isEmpty();
	}

	void addDependency(Object object) {
		dependencyManagerValue.get().inject(object);
	}

	void setDependencyManager(DependencyManager dependencyManager) {
		dependencyManagerValue.set(dependencyManager);
	}

	StateTransition getCurrentStateTransition() {
		return currentStateTransitionValue.get();
	}

	void injectStateIntoStateContext() {
		if(!stateContextValue.isEmpty()) {
			dispatchStateInject(stateContextValue.get());
		}
	}
}
