package com.github.thorbenkuck.keller.annotations;

import java.lang.annotation.*;

/**
 * This annotation is to used to signal, that a method-call is executing whatever it wants to do, within another Thread.
 *
 * This should be a sign for anyone, to ensure thread safety
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Asynchronous {
}
