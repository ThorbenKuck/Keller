package com.github.thorbenkuck.keller.event;

import com.github.thorbenkuck.keller.utility.Keller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

final class NativeAsynchronousDispatcherStrategy implements DispatcherStrategy {

	private final EventBus eventBus;
	private final ExecutorService threadPool;

	NativeAsynchronousDispatcherStrategy(EventBus eventBus, ExecutorService threadPool) {
		this.eventBus = eventBus;
		this.threadPool = threadPool;
	}

	private void dispatchAsynchronous(final List<EventBridge> list, final Object event) {
		if(list.isEmpty()) {
			return;
		}
		threadPool.execute(() -> {
			final List<Object> newEvents = new ArrayList<>();
			list.forEach(eventBridge -> {
				Object result = eventBridge.trigger(event);
				if(result != null) {
					newEvents.add(result);
				}
			});

			newEvents.forEach(eventBus::post);
		});
	}

	@Override
	public void dispatch(Object event, Collection<EventBridge> bridges) {
		Keller.parameterNotNull(event, bridges);
		final List<EventBridge> listings = bridges.stream()
				.filter(listening -> listening.isApplicable(event))
				.collect(Collectors.toList());

		dispatchAsynchronous(listings, event);
	}

	@Override
	public void dispatchDeadEvent(DeadEvent deadEvent, Collection<EventBridge> bridges) {
		final List<EventBridge> deadEventHandler = bridges.stream()
				.filter(listening -> listening.isApplicable(deadEvent))
				.collect(Collectors.toList());
		if(deadEventHandler.isEmpty()) {
			final Object originalEvent = deadEvent.getEvent();
			throw new DeadEventException("Found DeadEvent[" + originalEvent.getClass() + "]: " + originalEvent);
		}

		threadPool.execute(() -> deadEventHandler.forEach(listening -> listening.trigger(deadEvent)));
	}
}
