package com.github.thorbenkuck.keller.reactive;

@FunctionalInterface
public interface Subscriber<T> {

	void accept(T t);

}
