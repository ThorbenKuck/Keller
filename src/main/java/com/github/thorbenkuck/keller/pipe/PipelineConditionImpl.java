package com.github.thorbenkuck.keller.pipe;

import java.util.function.Predicate;

class PipelineConditionImpl<T> implements PipelineCondition<T> {
	private final APIPipelineElement<T> consumerPipelineElement;

	PipelineConditionImpl(PipelineElement<T> consumerPipelineElement) {
		// We define the implementation
		// We know the implementation
		// So we can do this.
		// All hail the implementation!
		this.consumerPipelineElement = (APIPipelineElement<T>) consumerPipelineElement;
	}

	@Override
	public PipelineChain<T> withRequirement(Predicate<T> predicate) {
		consumerPipelineElement.addCondition(predicate);
		return new PipelineChainImpl<>(this);
	}
}
