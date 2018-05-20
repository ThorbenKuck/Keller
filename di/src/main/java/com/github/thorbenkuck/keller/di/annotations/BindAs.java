package com.github.thorbenkuck.keller.di.annotations;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BindAs {

	Class<?>[] value();

}
