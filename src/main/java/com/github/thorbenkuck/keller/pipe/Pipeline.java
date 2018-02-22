package com.github.thorbenkuck.keller.pipe;

import com.github.thorbenkuck.keller.datatypes.interfaces.Closable;
import com.github.thorbenkuck.keller.datatypes.interfaces.GenericRunnable;
import com.github.thorbenkuck.keller.datatypes.interfaces.Lockable;

import java.util.function.Consumer;

public interface Pipeline<T> extends GenericRunnable<T>, Closable, Lockable {

	static <T> Pipeline<T> unifiedCreation() {
		return new QueuedPipeline<>();
	}

	PipelineCondition<T> addLast(Consumer<T> pipelineService);

	PipelineCondition<T> addFirst(Consumer<T> pipelineService);

	void remove(Consumer<T> pipelineService);
}
