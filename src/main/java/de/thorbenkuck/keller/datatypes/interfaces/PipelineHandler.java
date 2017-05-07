package de.thorbenkuck.keller.datatypes.interfaces;

@FunctionalInterface
public interface PipelineHandler<T> {
	void handle(T t);
}
