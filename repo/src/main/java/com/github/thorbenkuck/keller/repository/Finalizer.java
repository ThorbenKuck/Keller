package com.github.thorbenkuck.keller.repository;

import java.util.function.Consumer;

/**
 * If not present
 */
public interface Finalizer<T> extends Getter<T> {

	T run(Consumer<T> consumer);

	T run(Runnable runnable);

}
