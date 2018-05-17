package com.github.thorbenkuck.keller.pipe;

class NativePipelineChain<T> implements PipelineChain<T> {

	private final PipelineCondition<T> pipelineCondition;

	NativePipelineChain(final PipelineCondition<T> pipelineCondition) {
		this.pipelineCondition = pipelineCondition;
	}

	@Override
	public PipelineCondition<T> and() {
		return pipelineCondition;
	}
}
