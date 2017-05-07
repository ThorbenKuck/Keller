package de.thorbenkuck.keller.pipe;

import de.thorbenkuck.keller.datatypes.interfaces.PipelineHandler;

public interface Pipeline<T> {
	void addLast(PipelineHandler<T> pipelineHandler);

	void addFirst(PipelineHandler<T> pipelineHandler);

	void remove(PipelineHandler<T> pipelineHandler);

	void handle(T e);

	void setMode(PipelineModes pipelineMode);
}
