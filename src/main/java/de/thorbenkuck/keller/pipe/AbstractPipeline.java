package de.thorbenkuck.keller.pipe;

import de.thorbenkuck.keller.datatypes.interfaces.Handler;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

public abstract class AbstractPipeline<T, C extends Collection<PipelineService<T>>> implements Pipeline<T>, Serializable {

	protected final C core;
	protected final Lock coreLock = new ReentrantLock(true);
	protected final Map<Predicate<PipelineModes>, Handler<PipelineModes, C>> handler = new HashMap<>();

	protected AbstractPipeline(C c) {
		this.core = c;
	}

	@Override
	public void doPipeline(T element) {
		synchronized (coreLock) {
			try {
				coreLock.lock();
				core.forEach(pipelineService -> pipelineService.handle(element));
			} finally {
				coreLock.unlock();
			}
		}
	}
}
