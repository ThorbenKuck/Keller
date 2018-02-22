package com.github.thorbenkuck.keller.pipe;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public abstract class AbstractPipeline<T, C extends Collection<PipelineElement<T>>> implements Pipeline<T>, Serializable {

	private boolean closed = false;
	private final C core;
	private final Lock locker = new ReentrantLock(true);

	protected AbstractPipeline(C c) {
		this.core = c;
	}

	protected PipelineElement<T> createPipelineElement(Consumer<T> consumer) {
		return new PipelineElement<>(consumer);
	}

	@Override
	public final void run(T element) {
		assertIsOpen();
		synchronized (core) {
			core.forEach(pipelineService -> pipelineService.run(element));
		}
	}

	@Override
	public final void lock() {
		locker.lock();
	}

	@Override
	public final void unlock() {
		locker.unlock();
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

	protected C getCore() {
		synchronized (core) {
			return this.core;
		}
	}

	protected final void addPipelineElement(PipelineElement<T> element) {
		synchronized (core) {
			core.add(element);
		}
	}

	protected final void clearCore() {
		synchronized (core) {
			core.clear();
		}
	}

	protected final void addPipelineElements(Collection<PipelineElement<T>> elements) {
		synchronized (core) {
			core.addAll(elements);
		}
	}

	protected final void removePipelineElement(PipelineElement<T> element) {
		synchronized (core) {
			core.remove(element);
		}
	}
}
