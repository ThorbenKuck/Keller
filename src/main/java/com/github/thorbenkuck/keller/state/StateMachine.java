package com.github.thorbenkuck.keller.state;

import com.github.thorbenkuck.keller.di.DependencyManager;

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

	void start(Object object);

	void addDependency(Object object);

	void setDependencyManager(DependencyManager dependencyManager);
}
