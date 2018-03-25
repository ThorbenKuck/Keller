package com.github.thorbenkuck.keller.pipe;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AbstractPipeline<T, C extends Collection<PipelineElement<T>>> implements Pipeline<T>, Serializable {

	private final C core;
	private final Lock locker = new ReentrantLock(true);
	private boolean closed = false;

	protected AbstractPipeline(C c) {
		this.core = c;
	}

	@Override
	public final T apply(T element) {
		assertIsOpen();
		final Value current = new Value(element);
		synchronized (core) {
			core.forEach(pipelineService -> {
				// Done, to protect from potential problems
				// The JVM might still optimize it. That's
				// why temp is declared final.
				final T temp = current.get();
				current.set(pipelineService.apply(temp));
			});
		}

		return current.get();
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
		if (closed) {
			throw new RuntimeException("Pipeline has to be open!");
		}
	}

	@Override
	public int size() {
		return core.size();
	}

	@Override
	public void clear() {
		core.clear();
	}

	@Override
	public boolean isEmpty() {
		return core.isEmpty();
	}

	@Override
	public boolean contains(Function<T, T> pipelineService) {
		return core.contains(new FunctionPipelineElement<>(pipelineService));
	}

	@Override
	public boolean contains(Consumer<T> pipelineService) {
		return core.contains(new ConsumerPipelineElement<>(pipelineService));
	}

	protected PipelineCondition<T> createPipelineCondition(PipelineElement<T> pipelineElement) {
		return new PipelineConditionImpl<>(pipelineElement);
	}

	protected PipelineElement<T> createFunctionPipelineElement(Function<T, T> function) {
		return new FunctionPipelineElement<>(function);
	}

	protected PipelineElement<T> createConsumerPipelineElement(Consumer<T> consumer) {
		return new ConsumerPipelineElement<>(consumer);
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

	private final class Value {

		private T t;

		public Value(T t) {
			set(t);
		}

		public void set(T t) {
			this.t = t;
		}

		public T get() {
			return t;
		}
	}
}
