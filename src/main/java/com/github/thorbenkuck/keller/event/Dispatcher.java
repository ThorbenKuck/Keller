package com.github.thorbenkuck.keller.event;

import com.github.thorbenkuck.keller.utility.Keller;

import java.util.*;
import java.util.stream.Collectors;

class Dispatcher {

	private final Map<Class<?>, List<EventBridge>> mapping = new HashMap<>();

	private void add(EventBridge eventBridge) {
		synchronized (mapping) {
			mapping.computeIfAbsent(eventBridge.getEventType(), key -> new ArrayList<>());
			mapping.get(eventBridge.getEventType()).add(eventBridge);
		}
	}

	private List<EventBridge> get(Class<?> clazz) {
		final List<EventBridge> result;
		synchronized (mapping) {
			result = mapping.get(clazz);
		}

		return result;
	}

	private void dispatch(List<EventBridge> eventBridgeList, Object event) {
		if(eventBridgeList.isEmpty()) {
			dispatchDeadEvent(event);
		} else {
			final List<Object> newEvents = new ArrayList<>();
			eventBridgeList.forEach(listening -> {
				Object result = listening.trigger(event);
				if(result != null) {
					newEvents.add(result);
				}
			});
			newEvents.forEach(this::dispatch);
		}

	}

	private void dispatchDeadEvent(Object originalEvent) {
		final DeadEvent deadEvent = new DeadEvent(originalEvent);
		final List<EventBridge> deadEventHandler = get(DeadEvent.class).stream()
				.filter(listening -> listening.isApplicable(deadEvent))
				.collect(Collectors.toList());
		if(deadEventHandler.isEmpty()) {
			throw new DeadEventException("Found DeadEvent[" + originalEvent.getClass() + "]: " + originalEvent);
		}

		deadEventHandler.forEach(listening -> listening.trigger(deadEvent));
	}

	void unregister(Collection<EventBridge> collection) {
		for(EventBridge eventBridge : collection) {
			final List<EventBridge> stored = get(eventBridge.getEventType());
			stored.remove(eventBridge);
		}
	}

	void clear() {
		synchronized (mapping) {
			mapping.clear();
		}
	}

	void register(Collection<EventBridge> eventBridgeCollection) {
		for(EventBridge eventBridge : eventBridgeCollection) {
			add(eventBridge);
		}
	}

	void dispatch(Object event) {
		Keller.parameterNotNull(event);
		final List<EventBridge> toDispatch = get(event.getClass());
		List<EventBridge> listings = toDispatch.stream()
				.filter(listening -> listening.isApplicable(event))
				.collect(Collectors.toList());

		dispatch(listings, event);
	}
}
