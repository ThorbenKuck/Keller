package com.github.thorbenkuck.keller.event;

public interface EventBus {

	static EventBus create() {
		return new NativeEventBus();
	}

	void setDispatcherStrategy(final DispatcherStrategy dispatcherStrategy);

	void setCreationStrategy(final DispatcherCreationStrategy creationStrategy);

	void register(final Object object);

	void unregister(final Object object);

	void post(final Object object);

	void clear();

}