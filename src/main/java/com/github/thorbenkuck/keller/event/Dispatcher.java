package com.github.thorbenkuck.keller.event;

import com.github.thorbenkuck.keller.utility.Keller;

import java.util.*;

class Dispatcher {

	private final Map<Class<?>, List<Listening>> mapping = new HashMap<>();

	private void add(Listening listening) {
		synchronized (mapping) {
			mapping.computeIfAbsent(listening.getEventType(), key -> new ArrayList<>());
			mapping.get(listening.getEventType()).add(listening);
		}
	}

	private List<Listening> get(Class<?> clazz) {
		final List<Listening> result;
		synchronized (mapping) {
			result = mapping.get(clazz);
		}

		return result;
	}

	void register(Collection<Listening> listeningCollection) {
		for(Listening listening : listeningCollection) {
			add(listening);
		}
	}

	void dispatch(Object event) {
		Keller.parameterNotNull(event);
		final List<Listening> toDispatch = get(event.getClass());
		final List<Object> newEvents = new ArrayList<>();
		toDispatch.stream()
				.filter(listening -> listening.isApplicable(event))
				.forEach(listening -> {
					Object result = listening.trigger(event);
					if(result != null) {
						newEvents.add(result);
					}
				});

		newEvents.forEach(this::dispatch);
	}

	void unregister(Collection<Listening> collection) {
		for(Listening listening : collection) {
			final List<Listening> stored = get(listening.getEventType());
			stored.remove(listening);
		}
	}
}
