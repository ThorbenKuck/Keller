package de.thorbenkuck.keller.pipe;

import de.thorbenkuck.keller.datatypes.interfaces.Closable;
import de.thorbenkuck.keller.datatypes.interfaces.GenericRunnable;
import de.thorbenkuck.keller.datatypes.interfaces.Lockable;

import java.util.function.Consumer;

public interface Pipeline<T> extends GenericRunnable<T>, Closable, Lockable {

	static <T> Pipeline<T> unifiedCreation() {
		return new QueuedPipeline<>();
	}

	PipelineCondition<T> addLast(Consumer<T> pipelineService);

	PipelineCondition<T> addFirst(Consumer<T> pipelineService);

	void remove(Consumer<T> pipelineService);
}
