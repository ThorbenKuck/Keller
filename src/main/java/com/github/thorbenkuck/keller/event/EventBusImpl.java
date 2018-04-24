package com.github.thorbenkuck.keller.event;

class EventBusImpl implements EventBus {

	private final Dispatcher dispatcher = new Dispatcher();
	private final DispatcherStrategy strategy = new DispatcherStrategy();

	@Override
	public void register(Object object) {
		dispatcher.register(strategy.create(object));
	}

	@Override
	public void unregister(Object object) {
		dispatcher.unregister(strategy.create(object));
	}

	@Override
	public void post(Object object) {
		dispatcher.dispatch(object);
	}
}
