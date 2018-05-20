package com.github.thorbenkuck.keller.state.annotations;

import com.github.thorbenkuck.keller.state.StateMachine;

import java.lang.annotation.*;

/**
 * This Method defines the following State for any State within the {@link StateMachine}.
 *
 * A {@link StateMachine} will, after finishing the current State, search for the next Method, annotated with this Annotation.
 * If any Method with this annotation is present and has a non-primitive return value (ie. int [and Integer], float [and Float],
 * void [and Void]), the return value will be used as the next state. In any other case, as well if the annotated method
 * returns null, the {@link StateMachine} will finish.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface NextState {
}
