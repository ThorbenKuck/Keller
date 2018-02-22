package com.github.thorbenkuck.keller.pipe;

class PipelineChainImpl<T> implements PipelineChain<T> {

	private PipelineCondition<T> pipelineCondition;

	PipelineChainImpl(PipelineCondition<T> pipelineCondition) {
		this.pipelineCondition = pipelineCondition;
	}

	@Override
	public PipelineCondition<T> and() {
		return pipelineCondition;
	}
}
