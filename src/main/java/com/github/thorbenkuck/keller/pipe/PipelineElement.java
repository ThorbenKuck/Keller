package com.github.thorbenkuck.keller.pipe;

import java.util.function.Function;

public interface PipelineElement<T> extends Function<T, T> {
	@Override
	T apply(T t);

	default void encountered(Throwable t) {
		t.printStackTrace(System.out);
	}
}
