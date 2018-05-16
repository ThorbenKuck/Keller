package com.github.thorbenkuck.keller.di;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SingleInstanceOnly {
}
