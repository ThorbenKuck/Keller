package de.thorbenkuck.keller.pipe;

public interface PipelineChain<T> {

	PipelineCondition<T> and();

}
