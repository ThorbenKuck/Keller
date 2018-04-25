package com.github.thorbenkuck.keller.event;

public interface EventBridge {

	Object trigger(Object event);

	boolean isApplicable(Object event);

	Class<?> getEventType();
}
