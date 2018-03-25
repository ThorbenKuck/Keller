package com.github.thorbenkuck.keller.datatypes;

import com.github.thorbenkuck.keller.pipe.*;
import com.github.thorbenkuck.keller.utility.Keller;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Function;

public class QueuedPipeline<T> extends AbstractPipeline<T, Queue<PipelineElement<T>>> {

	public QueuedPipeline() {
		super(new LinkedList<>());
	}

	private void $addLast(PipelineElement<T> element) {
		try {
			assertIsOpen();
			lock();
			addPipelineElement(element);
		} finally {
			unlock();
		}
	}

	private void $addFirst(PipelineElement<T> element) {
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
	}

	private void $remove(PipelineElement<T> element) {
		try {
			assertIsOpen();
			lock();
			removePipelineElement(element);
		} finally {
			unlock();
		}
	}

	@Override
	public PipelineCondition<T> addLast(Consumer<T> consumer) {
		Keller.parameterNotNull(consumer);
		PipelineElement<T> element = createConsumerPipelineElement(consumer);
		$addLast(element);
		return createPipelineCondition(element);
	}

	@Override
	public PipelineCondition<T> addLast(final Function<T, T> pipelineService) {
		Keller.parameterNotNull(pipelineService);
		PipelineElement<T> element = createFunctionPipelineElement(pipelineService);
		$addLast(element);
		return createPipelineCondition(element);
	}

	@Override
	public PipelineCondition<T> addFirst(Consumer<T> consumer) {
		Keller.parameterNotNull(consumer);
		PipelineElement<T> element = createConsumerPipelineElement(consumer);
		$addFirst(element);
		return createPipelineCondition(element);
	}

	@Override
	public PipelineCondition<T> addFirst(final Function<T, T> pipelineService) {
		Keller.parameterNotNull(pipelineService);
		PipelineElement<T> element = createFunctionPipelineElement(pipelineService);
		$addFirst(element);
		return createPipelineCondition(element);
	}

	@Override
	public void remove(Consumer<T> consumer) {
		$remove(createConsumerPipelineElement(consumer));
	}

	@Override
	public void remove(Function<T, T> function) {
		$remove(createFunctionPipelineElement(function));
	}
}
