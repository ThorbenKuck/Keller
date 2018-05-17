package com.github.thorbenkuck.keller.event;

import com.github.thorbenkuck.keller.utility.Keller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

final class NativeDispatcherStrategy implements DispatcherStrategy {

	private final EventBus eventBus;

	NativeDispatcherStrategy(final EventBus eventBus) {
		this.eventBus = eventBus;
	}

	private void dispatch(final List<EventBridge> eventBridgeList, final Object event) {
		final List<Object> newEvents = new ArrayList<>();
		eventBridgeList.forEach(listening -> {
			Object result = listening.trigger(event);
			if(result != null) {
				newEvents.add(result);
			}
		});

		newEvents.forEach(eventBus::post);
	}

	@Override
	public void dispatch(final Object event, final Collection<EventBridge> bridges) {
		Keller.parameterNotNull(event);
		final List<EventBridge> listings = bridges.stream()
				.filter(listening -> listening.isApplicable(event))
				.collect(Collectors.toList());

		dispatch(listings, event);
	}

	@Override
	public void dispatchDeadEvent(final DeadEvent deadEvent, final Collection<EventBridge> bridges) {
		final List<EventBridge> deadEventHandler = bridges.stream()
				.filter(listening -> listening.isApplicable(deadEvent))
				.collect(Collectors.toList());
		if(deadEventHandler.isEmpty()) {
			final Object originalEvent = deadEvent.getEvent();
			throw new DeadEventException("Found DeadEvent[" + originalEvent.getClass() + "]: " + originalEvent);
		}

		deadEventHandler.forEach(listening -> listening.trigger(deadEvent));
	}
}
