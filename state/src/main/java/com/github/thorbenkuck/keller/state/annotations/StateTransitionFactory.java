package com.github.thorbenkuck.keller.state.annotations;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface StateTransitionFactory {
}
