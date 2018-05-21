package com.github.thorbenkuck.keller.state;

import com.github.thorbenkuck.keller.di.DependencyManager;

import java.util.function.Consumer;

/**
 * The StateMachine allows for a sequential execution of different States.
 *
 * It may be used as a Command-pattern (if all States only have one following State), or as a correct State-pattern.
 *
 * The sequence of States is hereby defined by the states itself. Any State defines the following State. Thereby it is
 * mandatory, that the State-Sequence is correctly defined.
 */
public interface StateMachine {

	static StateMachine create() {
		return new NativeStateMachine();
	}

	boolean isRunning();

	void setStateContext(Object object);

	void stop();

	void start(Object object);

	void parallel(Object object);

	void addFinishedCallback(Consumer<StateMachine> callback);

	void addStateDependency(Object object);

	void setDependencyManager(DependencyManager dependencyManager);

	void step();
}
