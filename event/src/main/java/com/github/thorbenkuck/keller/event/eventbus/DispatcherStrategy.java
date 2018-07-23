package com.github.thorbenkuck.keller.event.eventbus;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public interface DispatcherStrategy {

	static void applyDefault(final EventBus eventBus) {
		final DispatcherStrategy dispatcherStrategy = new NativeDispatcherStrategy(eventBus);
		eventBus.setDispatcherStrategy(dispatcherStrategy);
	}

	static void applyParallel(final EventBus eventBus) {
		final DispatcherStrategy dispatcherStrategy = new NativeParallelDispatcherStrategy(eventBus);
		eventBus.setDispatcherStrategy(dispatcherStrategy);
	}

	static void applyAsynchronous(final EventBus eventBus) {
		applyAsynchronous(eventBus, Executors.newCachedThreadPool());
	}

	static void applyAsynchronous(final EventBus eventBus, final ExecutorService threadPool) {
		final DispatcherStrategy dispatcherStrategy = new NativeAsynchronousDispatcherStrategy(eventBus, threadPool);
		eventBus.setDispatcherStrategy(dispatcherStrategy);
	}

	void dispatch(final Object event, final Collection<EventBridge> bridges);

	void dispatchDeadEvent(final DeadEvent deadEvent, final Collection<EventBridge> bridges);
}
