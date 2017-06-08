package de.thorbenkuck.keller.pipe;

import java.util.function.Predicate;

public class PipelineConditionImpl<T> implements PipelineCondition<T> {
	private final PipelineElement<T> pipelineElement;

	public PipelineConditionImpl(PipelineElement<T> pipelineElement) {
		this.pipelineElement = pipelineElement;
	}

	@Override
	public PipelineChain<T> withRequirement(Predicate<T> predicate) {
		pipelineElement.addCondition(predicate);
		return new PipelineChainImpl<>(this);
	}
}
