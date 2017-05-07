package de.thorbenkuck.keller.pipe;

public interface Pipeline<T> {
	void addLast(PipelineService<T> pipelineService);

	void addFirst(PipelineService<T> pipelineService);

	void remove(PipelineService<T> pipelineService);

	void doPipeline(T e);
}
