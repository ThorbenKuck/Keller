package com.github.thorbenkuck.keller.datatypes;

import com.github.thorbenkuck.keller.pipe.AbstractPipeline;
import com.github.thorbenkuck.keller.pipe.PipelineCondition;
import com.github.thorbenkuck.keller.pipe.PipelineConditionImpl;
import com.github.thorbenkuck.keller.pipe.PipelineElement;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

public class QueuedPipeline<T> extends AbstractPipeline<T, Queue<PipelineElement<T>>> {

	public QueuedPipeline() {
		super(new LinkedList<>());
	}

	@Override
	public PipelineCondition<T> addLast(Consumer<T> consumer) {
		PipelineElement<T> element = createPipelineElement(consumer);
		try {
			assertIsOpen();
			lock();
			addPipelineElement(element);
		} finally {
			unlock();
		}
		return new PipelineConditionImpl<>(element);
	}

	@Override
	public PipelineCondition<T> addFirst(Consumer<T> consumer) {
		PipelineElement<T> element = createPipelineElement(consumer);
		Queue<PipelineElement<T>> newCore = new LinkedList<>();
		newCore.add(element);
		Queue<PipelineElement<T>> core = getCore();
		try {
			assertIsOpen();
			lock();
			newCore.addAll(core);
			clearCore();
			addPipelineElements(newCore);
		} finally {
			unlock();
		}
		return new PipelineConditionImpl<>(element);
	}

	@Override
	public void remove(Consumer<T> consumer) {
		try {
			assertIsOpen();
			lock();
			removePipelineElement(createPipelineElement(consumer));
		} finally {
			unlock();
		}
	}
}
