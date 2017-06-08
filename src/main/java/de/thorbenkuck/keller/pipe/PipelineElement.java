package de.thorbenkuck.keller.pipe;

import de.thorbenkuck.keller.datatypes.interfaces.GenericRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

class PipelineElement<T> implements GenericRunnable<T> {

	private final Consumer<T> consumer;
	private final List<Predicate<T>> predicates = new ArrayList<>();

	PipelineElement(Consumer<T> consumer) {
		this.consumer = consumer;
	}

	@Override
	public void run(T t) {
		if (test(t)) {
			getConsumer().accept(t);
		}
	}

	boolean test(T t) {
		for (Predicate<T> predicate : predicates) {
			if (! predicate.test(t)) {
				return false;
			}
		}
		return true;
	}

	Consumer<T> getConsumer() {
		return consumer;
	}

	void addCondition(Predicate<T> predicate) {
		predicates.add(predicate);
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj.getClass().equals(PipelineElement.class) && consumer.equals(((PipelineElement) obj).getConsumer());
	}
}
