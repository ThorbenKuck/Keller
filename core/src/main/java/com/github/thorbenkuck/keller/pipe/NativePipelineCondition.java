package com.github.thorbenkuck.keller.pipe;

import java.util.function.Predicate;

class NativePipelineCondition<T> implements PipelineCondition<T> {

	private final APIPipelineElement<T> consumerPipelineElement;

	NativePipelineCondition(final PipelineElement<T> consumerPipelineElement) {
		// We define the implementation
		// We know the implementation
		// So we can do this.
		// All hail the implementation!
		this.consumerPipelineElement = (APIPipelineElement<T>) consumerPipelineElement;
	}

	@Override
	public PipelineChain<T> withRequirement(final Predicate<T> predicate) {
		consumerPipelineElement.addCondition(predicate);
		return new NativePipelineChain<>(this);
	}
}
