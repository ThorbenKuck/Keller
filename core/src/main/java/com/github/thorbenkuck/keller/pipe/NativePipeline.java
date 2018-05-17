package com.github.thorbenkuck.keller.pipe;

import com.github.thorbenkuck.keller.utility.Keller;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Function;

public final class NativePipeline<T> extends AbstractPipeline<T, Queue<PipelineElement<T>>> {

	public NativePipeline() {
		super(new ArrayDeque<>());
	}

	private void addLast0(final PipelineElement<T> element) {
		try {
			assertIsOpen();
			lock();
			addPipelineElement(element);
		} finally {
			unlock();
		}
	}

	private void addFirst0(final PipelineElement<T> element) {
		final Queue<PipelineElement<T>> newCore = new LinkedList<>();
		newCore.add(element);
		final Queue<PipelineElement<T>> core = getCore();
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

	private void remove0(final PipelineElement<T> element) {
		try {
			assertIsOpen();
			lock();
			removePipelineElement(element);
		} finally {
			unlock();
		}
	}

	@Override
	public PipelineCondition<T> addLast(final Consumer<T> consumer) {
		Keller.parameterNotNull(consumer);
		final PipelineElement<T> element = createConsumerPipelineElement(consumer);
		addLast0(element);
		return createPipelineCondition(element);
	}

	@Override
	public PipelineCondition<T> addLast(final Function<T, T> pipelineService) {
		Keller.parameterNotNull(pipelineService);
		final PipelineElement<T> element = createFunctionPipelineElement(pipelineService);
		addLast0(element);
		return createPipelineCondition(element);
	}

	@Override
	public PipelineCondition<T> addFirst(final Consumer<T> consumer) {
		Keller.parameterNotNull(consumer);
		final PipelineElement<T> element = createConsumerPipelineElement(consumer);
		addFirst0(element);
		return createPipelineCondition(element);
	}

	@Override
	public PipelineCondition<T> addFirst(final Function<T, T> pipelineService) {
		Keller.parameterNotNull(pipelineService);
		final PipelineElement<T> element = createFunctionPipelineElement(pipelineService);
		addFirst0(element);
		return createPipelineCondition(element);
	}

	@Override
	public void remove(final Consumer<T> consumer) {
		remove0(createConsumerPipelineElement(consumer));
	}

	@Override
	public void remove(final Function<T, T> function) {
		remove0(createFunctionPipelineElement(function));
	}
}
