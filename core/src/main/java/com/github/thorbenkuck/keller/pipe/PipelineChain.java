package com.github.thorbenkuck.keller.pipe;

public interface PipelineChain<T> {

	PipelineCondition<T> and();

}
