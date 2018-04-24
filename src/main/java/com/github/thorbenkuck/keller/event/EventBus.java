package com.github.thorbenkuck.keller.event;

public interface EventBus {

	static EventBus create() {
		return new EventBusImpl();
	}

	static EventBus parallel() {
		return new ParallelEventBus();
	}

	void register(Object object);

	void unregister(Object object);

	void post(Object object);

}
