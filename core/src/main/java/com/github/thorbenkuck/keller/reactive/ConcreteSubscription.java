package com.github.thorbenkuck.keller.reactive;

public interface ConcreteSubscription<T> extends Subscription {

	void notify(T t);

}
