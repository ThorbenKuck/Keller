package com.github.thorbenkuck.keller.pipe;

import com.github.thorbenkuck.keller.datatypes.interfaces.Closable;
import com.github.thorbenkuck.keller.datatypes.interfaces.Lockable;

import java.util.function.Consumer;
import java.util.function.Function;

public interface Pipeline<T> extends Function<T, T>, Closable, Lockable {

	static <T> Pipeline<T> unifiedCreation() {
		return new NativePipeline<>();
	}

	PipelineCondition<T> addLast(final Consumer<T> pipelineService);

	PipelineCondition<T> addLast(final Function<T, T> pipelineService);

	PipelineCondition<T> addFirst(final Consumer<T> pipelineService);

	PipelineCondition<T> addFirst(final Function<T, T> pipelineService);

	void remove(final Consumer<T> pipelineService);

	void remove(final Function<T, T> pipelineService);

	int size();

	void clear();

	boolean isEmpty();

	boolean contains(final Function<T, T> pipelineService);

	boolean contains(final Consumer<T> pipelineService);

	default PipelineCondition<T> add(final Consumer<T> pipelineService) {
		return addFirst(pipelineService);
	}

	default PipelineCondition<T> add(final Function<T, T> pipelineService) {
		return addFirst(pipelineService);
	}

	default void ifClosed(final Consumer<Pipeline> consumer) {
		if (isClosed()) {
			consumer.accept(this);
		}
	}

	default void ifOpen(final Consumer<Pipeline> consumer) {
		if (!isClosed()) {
			consumer.accept(this);
		}
	}
}
