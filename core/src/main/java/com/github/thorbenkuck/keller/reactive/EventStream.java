package com.github.thorbenkuck.keller.reactive;

public interface EventStream<T> {

	Subscription subscribe(Subscriber<T> subscriber);

}
