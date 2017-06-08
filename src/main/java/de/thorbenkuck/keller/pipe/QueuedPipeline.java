package de.thorbenkuck.keller.pipe;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

public class QueuedPipeline<T> extends AbstractPipeline<T, Queue<PipelineElement<T>>> implements Pipeline<T> {

	private boolean closed = false;

	public QueuedPipeline() {
		super(new LinkedList<>());
	}

	@Override
	public PipelineCondition<T> addLast(Consumer<T> consumer) {
		PipelineElement<T> element = new PipelineElement<>(consumer);
		try {
			assertIsOpen();
			lock();
			getCore().add(element);
		} finally {
			unlock();
		}
		return new PipelineConditionImpl<>(element);
	}

	@Override
	public PipelineCondition<T> addFirst(Consumer<T> consumer) {
		PipelineElement<T> element = new PipelineElement<>(consumer);
		Queue<PipelineElement<T>> newCore = new LinkedList<>();
		newCore.add(element);
		Queue<PipelineElement<T>> core = getCore();
		try {
			assertIsOpen();
			lock();
			newCore.addAll(core);
			core.clear();
			core.addAll(newCore);
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
			getCore().remove(new PipelineElement<>(consumer));
		} finally {
			unlock();
		}
	}

	@Override
	public void close() {
		lock();
		closed = true;
	}

	@Override
	public void open() {
		unlock();
		closed = false;
	}

	@Override
	public boolean isClosed() {
		return closed;
	}

	@Override
	public void assertIsOpen() {
		if(closed) {
			throw new RuntimeException("Pipeline has to be open!");
		}
	}
}
