package com.github.thorbenkuck.keller.reactive;

import java.util.List;

public class SimpleEventStream<T> extends AbstractEventStream<T> {
	@Override
	protected void dispatch(List<ConcreteSubscription<T>> concreteSubscriptions, T t) {
		concreteSubscriptions.forEach(concreteSubscription -> concreteSubscription.notify(t));
	}
}
