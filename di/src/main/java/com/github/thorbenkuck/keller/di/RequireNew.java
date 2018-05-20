package com.github.thorbenkuck.keller.di;

import java.lang.annotation.*;

/**
 * This Annotation signals the {@link com.github.thorbenkuck.keller.di.DependencyManager}, that the annotated Parameter
 * is required to be instantiated.
 *
 * This Annotation override the {@link SingleInstanceOnly} annotation, which in itself is to signal an Singleton. This means,
 * that even if any instance is already created, this parameter will be created again.
 *
 * This might not bee, what you want. In most cases, this Annotation should not be used. However, there are some rare
 * cases, where this is needed.
 *
 * In most cases, this Annotation will enforce an behaviour, that is the opposite of what you want. Some variables might
 * be instantiated at a time, they should not be instantiated. Use this Annotation with great care.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RequireNew {
}
