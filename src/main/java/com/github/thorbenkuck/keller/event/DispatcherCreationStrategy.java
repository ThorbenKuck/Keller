package com.github.thorbenkuck.keller.event;

import java.util.Collection;

public interface DispatcherCreationStrategy {
	Collection<EventBridge> create(final Object object);
}
