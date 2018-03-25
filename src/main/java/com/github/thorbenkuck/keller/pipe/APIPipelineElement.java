package com.github.thorbenkuck.keller.pipe;

import java.util.function.Predicate;

interface APIPipelineElement<T> extends PipelineElement<T> {

	void addCondition(Predicate<T> predicate);

}
