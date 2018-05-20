package com.github.thorbenkuck.keller.pipe;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.keller.observers.ObservableValue;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
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

	private void run(final PipelineElement<T>[] collection, final Value<T> value) {
		if(collection.length == 0) {
			return;
		}
		for (PipelineElement<T> pipelineService : collection) {
			// Done, to protect from potential problems
			// The JVM might still optimize it. That's
			// why temp is declared final.
			final T temp = value.get();
			try {
				T newTemp = pipelineService.apply(temp);
				value.set(newTemp);
			} catch (Throwable t) {
				pipelineService.encountered(t);
			}
		}
	}

	// What is up with your
	// generic system java..
	// Why? This inconsistency...
	@SuppressWarnings("unchecked")
	@Override
	public final T apply(final T element) {
		assertIsOpen();
		final Value<T> value = Value.of(element);
		final PipelineElement<T>[] elements;
		synchronized (core) {
			elements = new ArrayList<>(core).toArray(new PipelineElement[core.size()]);
		}

		run(elements, value);

		return value.get();
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
		synchronized (core) {
			return core.contains(new FunctionPipelineElement<>(pipelineService));
		}
	}

	@Override
	public boolean contains(Consumer<T> pipelineService) {
		synchronized (core) {
			return core.contains(new ConsumerPipelineElement<>(pipelineService));
		}
	}

	@Override
	public String toReadable() {
		final Queue<PipelineElement<T>> copy;
		synchronized (core) {
			copy = new LinkedList<>(core);
		}

		final StringBuilder builder = new StringBuilder();
		while(copy.peek() != null) {
			builder.append(copy.poll());
			if(copy.peek() != null) {
				builder.append(" => ");
			}
		}

		return builder.toString();
	}

	protected PipelineCondition<T> createPipelineCondition(PipelineElement<T> pipelineElement) {
		return new NativePipelineCondition<>(pipelineElement);
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
}
