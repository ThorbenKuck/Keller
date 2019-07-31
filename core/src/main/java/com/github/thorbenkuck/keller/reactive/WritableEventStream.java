package com.github.thorbenkuck.keller.reactive;

public interface WritableEventStream<T> extends EventStream<T> {

	void push(T t);

}
