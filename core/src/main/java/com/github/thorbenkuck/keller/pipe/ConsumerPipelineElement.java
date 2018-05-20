package com.github.thorbenkuck.keller.pipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ConsumerPipelineElement<T> implements APIPipelineElement<T> {

	private final Consumer<T> consumer;
	private final List<Predicate<T>> predicates = new ArrayList<>();

	ConsumerPipelineElement(final Consumer<T> consumer) {
		this.consumer = consumer;
	}

	@Override
	public T apply(T t) {
		if (test(t)) {
			getConsumer().accept(t);
		}

		return t;
	}

	@Override
	public void addCondition(final Predicate<T> predicate) {
		predicates.add(predicate);
	}

	@Override
	public boolean equals(final Object obj) {
		return obj != null && obj.getClass().equals(ConsumerPipelineElement.class) && consumer.equals(((ConsumerPipelineElement) obj).getConsumer());
	}

	@Override
	public String toString() {
		return consumer.toString();
	}

	boolean test(final T t) {
		for (final Predicate<T> predicate : predicates) {
			if (! predicate.test(t)) {
				return false;
			}
		}
		return true;
	}

	Consumer<T> getConsumer() {
		return consumer;
	}
}
