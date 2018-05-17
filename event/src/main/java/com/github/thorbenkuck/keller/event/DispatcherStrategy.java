package com.github.thorbenkuck.keller.event;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public interface DispatcherStrategy {

	static void applyDefault(final EventBus eventBus) {
		final DispatcherStrategy dispatcherStrategy = new NativeDispatcherStrategy(eventBus);
		eventBus.setDispatcherStrategy(dispatcherStrategy);
	}

	static void parallel(final EventBus eventBus) {
		final DispatcherStrategy dispatcherStrategy = new NativeParallelDispatcherStrategy(eventBus);
		eventBus.setDispatcherStrategy(dispatcherStrategy);
	}

	static void asynchronous(final EventBus eventBus) {
		asynchronous(eventBus, Executors.newCachedThreadPool());
	}

	static void asynchronous(final EventBus eventBus, final ExecutorService threadPool) {
		final DispatcherStrategy dispatcherStrategy = new NativeAsynchronousDispatcherStrategy(eventBus, threadPool);
		eventBus.setDispatcherStrategy(dispatcherStrategy);
	}

	void dispatch(final Object event, final Collection<EventBridge> bridges);

	void dispatchDeadEvent(final DeadEvent deadEvent, final Collection<EventBridge> bridges);
}
