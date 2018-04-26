package com.github.thorbenkuck.keller.event;

import com.github.thorbenkuck.keller.utility.Keller;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class NativeDispatcherCreationStrategy implements DispatcherCreationStrategy {

	private EventBridge createHook(final Method method, final Object object) {
		final Hook hook = method.getAnnotation(Hook.class);
		if(hook == null) {
			throw new IllegalArgumentException("No Hook annotation found!");
		}
		if(!hook.active()) {
			return null;
		}
		if(method.getParameterCount() != 1) {
			throw new IllegalArgumentException("Hook has to be added to any method with exactly one argument");
		}
		return new EventBridgeHook(object, method);
	}

	private EventBridge createListen(final Method method, final Object object) {
		final Listen listen = method.getAnnotation(Listen.class);
		if(listen == null) {
			throw new IllegalArgumentException("No Hook annotation found!");
		}
		if(!listen.active()) {
			return null;
		}
		if(method.getParameterCount() != 1) {
			throw new IllegalArgumentException("Hook has to be added to any method with exactly one argument");
		}
		return new EventBridgeListener(object, method);
	}

	@Override
	public Collection<EventBridge> create(final Object object) {
		Keller.parameterNotNull(object);
		final List<EventBridge> result = new ArrayList<>();
		for(final Method method : object.getClass().getDeclaredMethods()) {
			if(method.getAnnotation(Hook.class) != null){
				final EventBridge eventBridge = createHook(method, object);
				if(eventBridge != null) {
					result.add(eventBridge);
				}
			} else if(method.getAnnotation(Listen.class) != null) {
				final EventBridge eventBridge = createListen(method, object);
				if(eventBridge != null) {
					result.add(eventBridge);
				}
			}
		}
		return result;
	}

}
