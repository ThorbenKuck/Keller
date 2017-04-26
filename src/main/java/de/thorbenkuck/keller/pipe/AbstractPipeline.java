package de.thorbenkuck.keller.pipe;

import de.thorbenkuck.keller.datatypes.interfaces.Acceptor;
import de.thorbenkuck.keller.datatypes.interfaces.Handler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

public abstract class AbstractPipeline<T, C extends Collection<Acceptor<T>>> implements Pipeline<T> {

	protected final C core;
	protected final Lock coreLock = new ReentrantLock(true);
	protected final Map<Predicate<PipelineModes>, Handler<PipelineModes, C>> handler = new HashMap<>();
	protected PipelineModes mode = PipelineModes.OPEN;

	protected AbstractPipeline(C c) {
		this.core = c;
	}

	protected boolean acceptableToCore(Acceptor<T> acceptor) {
		return mode == PipelineModes.OPEN || core.contains(acceptor);
	}

	@Override
	public void setMode(PipelineModes pipelineMode) {
		synchronized (core) {
			handler.keySet()
					.stream()
					.filter(pipelineModesPredicate -> pipelineModesPredicate.test(pipelineMode))
					.forEach(pipelineModesPredicate -> handler.get(pipelineModesPredicate).handle(pipelineMode, core));
		}
	}

	@Override
	public void call(T element) {
		synchronized (coreLock) {
			try {
				coreLock.lock();
				core.forEach(acceptor -> acceptor.accept(element));
			} finally {
				coreLock.unlock();
			}
		}
	}
}
