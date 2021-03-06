package com.github.thorbenkuck.keller.event.eventbus.annotations;

import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Hook {

	boolean active() default true;

}
