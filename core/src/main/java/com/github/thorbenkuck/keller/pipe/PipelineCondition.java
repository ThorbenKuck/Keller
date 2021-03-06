package com.github.thorbenkuck.keller.pipe;

import java.util.function.Predicate;

public interface PipelineCondition<T> {

	PipelineChain<T> withRequirement(final Predicate<T> predicate);

}
