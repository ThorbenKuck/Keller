package com.github.thorbenkuck.keller.annotations;

import java.lang.annotation.*;

/**
 * This annotation is to used to signal, that a method-call is executing whatever it wants to do, within another Thread.
 *
 * This should be a sign for anyone, to ensure thread safety
 *
 * Outside of NetCom2s internal Modules, this Annotation is a signal, that you will be seduced to asynchronous behaviour
 * if you call this Method. Therefor you may not be able to work synchronous or in an procedural style.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Asynchronous {
}
