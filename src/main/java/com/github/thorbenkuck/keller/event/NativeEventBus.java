package com.github.thorbenkuck.keller.event;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.keller.utility.Keller;

import java.util.*;

final class NativeEventBus implements EventBus {

	private final Value<DispatcherStrategy> dispatcherStrategy = Value.synchronize(new NativeDispatcherStrategy(this));
	private final Value<DispatcherCreationStrategy> creationStrategy = Value.synchronize(new NativeDispatcherCreationStrategy());
	private final Map<Class<?>, List<EventBridge>> mapping = new HashMap<>();

	private void add(final EventBridge eventBridge) {
		synchronized (mapping) {
			mapping.computeIfAbsent(eventBridge.getEventType(), key -> new ArrayList<>());
			mapping.get(eventBridge.getEventType()).add(eventBridge);
		}
	}

	private List<EventBridge> copyEventBridges(final Class<?> clazz) {
		final List<EventBridge> result = new ArrayList<>();
		synchronized (mapping) {
			result.addAll(mapping.get(clazz));
		}

		return result;
	}

	private List<EventBridge> getEventBridges(final Class<?> clazz) {
		final List<EventBridge> result;
		synchronized (mapping) {
			result = mapping.get(clazz);
		}

		return result != null ? result : new ArrayList<>();
	}

	private DispatcherStrategy getDispatcherStrategy() {
		return dispatcherStrategy.get();
	}

	private void unregister(final Collection<EventBridge> collection) {
		for(final EventBridge eventBridge : collection) {
			final List<EventBridge> stored = getEventBridges(eventBridge.getEventType());
			stored.remove(eventBridge);
		}
	}

	private void register(final Collection<EventBridge> eventBridgeCollection) {
		for(final EventBridge eventBridge : eventBridgeCollection) {
			add(eventBridge);
		}
	}

	private void dispatchDeadEvent(final Object event) {
		final DeadEvent deadEvent = new DeadEvent(event);
		final List<EventBridge> bridges = copyEventBridges(deadEvent.getClass());
		getDispatcherStrategy().dispatchDeadEvent(deadEvent, bridges);
	}

	private boolean tryDispatch(final Object object) {
		Keller.parameterNotNull(object);
		final List<EventBridge> bridges = copyEventBridges(object.getClass());
		if(bridges.isEmpty()) {
			return false;
		}
		getDispatcherStrategy().dispatch(object, bridges);
		return true;
	}

	@Override
	public void setDispatcherStrategy(final DispatcherStrategy dispatcherStrategy) {
		this.dispatcherStrategy.set(dispatcherStrategy);
	}

	@Override
	public void setCreationStrategy(final DispatcherCreationStrategy creationStrategy) {
		this.creationStrategy.set(creationStrategy);
	}

	@Override
	public void register(final Object object) {
		register(creationStrategy.get().create(object));
	}

	@Override
	public void unregister(final Object object) {
		unregister(creationStrategy.get().create(object));
	}

	@Override
	public void post(final Object object) {
		if(!tryDispatch(object)) {
			dispatchDeadEvent(object);
		}
	}

	@Override
	public void clear() {
		synchronized (mapping) {
			mapping.clear();
		}
	}
}
