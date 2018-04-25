package com.github.thorbenkuck.keller.event;

public final class DeadEvent {

	private final Object event;

	DeadEvent(Object event) {
		this.event = event;
	}

	public final Object getEvent() {
		return event;
	}
}
