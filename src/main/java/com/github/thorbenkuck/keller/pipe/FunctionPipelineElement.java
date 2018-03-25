package com.github.thorbenkuck.keller.pipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class FunctionPipelineElement<T> implements APIPipelineElement<T> {

	private final Function<T, T> function;
	private final List<Predicate<T>> predicates = new ArrayList<>();

	FunctionPipelineElement(final Function<T, T> function) {
		this.function = function;
	}

	@Override
	public T apply(T t) {
		if (test(t)) {
			getFunction().apply(t);
		}

		return t;
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj.getClass().equals(FunctionPipelineElement.class) && function.equals(((FunctionPipelineElement) obj).getFunction());
	}

	@Override
	public void addCondition(Predicate<T> predicate) {
		predicates.add(predicate);
	}

	boolean test(T t) {
		for (Predicate<T> predicate : predicates) {
			if (! predicate.test(t)) {
				return false;
			}
		}
		return true;
	}

	Function<T, T> getFunction() {
		return function;
	}
}
