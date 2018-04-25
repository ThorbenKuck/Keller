package com.github.thorbenkuck.keller.event;

import java.util.Collection;

class EventBusImpl implements EventBus {

	private final Dispatcher dispatcher = new Dispatcher();
	private final DispatcherCreationStrategy strategy = new DispatcherCreationStrategy();

	private void register(Collection<EventBridge> collection) {
		dispatcher.register(collection);
	}

	@Override
	public void register(Object object) {
		Collection<EventBridge> eventBridge = strategy.createListener(object);
		register(eventBridge);
	}

	@Override
	public void hook(Object object) {
		Collection<EventBridge> eventBridge = strategy.createListener(object);
		register(eventBridge);
	}

	@Override
	public void unregister(Object object) {
		dispatcher.unregister(strategy.createListener(object));
	}

	public void unhook(Object object) {
		dispatcher.unregister(strategy.createHook(object));
	}

	@Override
	public void post(Object object) {
		dispatcher.dispatch(object);
	}

	@Override
	public void clear() {
		dispatcher.clear();
	}
}
