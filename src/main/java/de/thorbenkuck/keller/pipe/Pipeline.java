package de.thorbenkuck.keller.pipe;

public interface Pipeline<T> {
	void addLast(T t);

	void addFirst(T t);

	void remove(T t);

	void setMode(PipelineModes pipelineMode);
}
