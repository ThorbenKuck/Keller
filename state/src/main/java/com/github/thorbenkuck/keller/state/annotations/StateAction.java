package com.github.thorbenkuck.keller.state.annotations;

import com.github.thorbenkuck.keller.state.StateMachine;

import java.lang.annotation.*;

/**
 * This Annotation signals the {@link StateMachine}, that the annotated Method is used for executing the current State.
 *
 * Any Method might be annotated with this annotation. It does not matter, what dependencies are required. Any required
 * dependency, that is requested by the annotated method as an parameter, has to be added to the {@link StateMachine} via
 * {@link StateMachine#addStateDependency(Object)}.
 *
 * If the required dependency is not set, the {@link StateMachine} might use an internally maintained {@link com.github.thorbenkuck.keller.di.DependencyManager}
 * to create the missing dependency.
 *
 * @see StateMachine
 * @see com.github.thorbenkuck.keller.di.DependencyManager
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface StateAction {
}
