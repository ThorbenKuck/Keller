package de.thorbenkuck.keller.pipe;

public interface Pipeline<T> {

	static <T> Pipeline<T> unifiedCreation() {
		return new QueuedPipeline<>();
	}

	void addLast(PipelineService<T> pipelineService);

	void addFirst(PipelineService<T> pipelineService);

	void remove(PipelineService<T> pipelineService);

	void doPipeline(T e);
}
