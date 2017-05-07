package de.thorbenkuck.keller.pipe;

@FunctionalInterface
public interface PipelineService<T> {
	void handle(T t);
}
