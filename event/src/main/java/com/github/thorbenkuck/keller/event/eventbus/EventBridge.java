package com.github.thorbenkuck.keller.event.eventbus;

public interface EventBridge {

	Object trigger(final Object event);

	Object getSource();

	boolean isApplicable(final Object event);

	Class<?> getEventType();
}
