package com.github.thorbenkuck.keller.pipe;

import com.github.thorbenkuck.keller.datatypes.QueuedPipeline;
import com.github.thorbenkuck.keller.datatypes.interfaces.Closable;
import com.github.thorbenkuck.keller.datatypes.interfaces.GenericRunnable;
import com.github.thorbenkuck.keller.datatypes.interfaces.Lockable;

import java.util.function.Consumer;
import java.util.function.Function;

public interface Pipeline<T> extends Function<T, T>, Closable, Lockable {

	static <T> Pipeline<T> unifiedCreation() {
		return new QueuedPipeline<>();
	}

	PipelineCondition<T> addLast(Consumer<T> pipelineService);

	PipelineCondition<T> addLast(Function<T, T> pipelineService);

	PipelineCondition<T> addFirst(Consumer<T> pipelineService);

	PipelineCondition<T> addFirst(Function<T, T> pipelineService);

	void remove(Consumer<T> pipelineService);

	void remove(Function<T, T> pipelineService);

	int size();

	void clear();

	boolean isEmpty();

	boolean contains(Function<T, T> pipelineService);

	boolean contains(Consumer<T> pipelineService);

	default void ifClosed(Consumer<Pipeline> consumer) {
		if(isClosed()) {
			consumer.accept(this);
		}
	}

	default void ifOpen(Consumer<Pipeline> consumer) {
		if(!isClosed()) {
			consumer.accept(this);
		}
	}
}
