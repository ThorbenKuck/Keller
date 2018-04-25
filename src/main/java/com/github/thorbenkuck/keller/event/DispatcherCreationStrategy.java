package com.github.thorbenkuck.keller.event;

import com.github.thorbenkuck.keller.utility.Keller;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class DispatcherCreationStrategy {

	Collection<EventBridge> createListener(Object object) {
		Keller.parameterNotNull(object);
		final List<EventBridge> result = new ArrayList<>();
		for(Method method : object.getClass().getDeclaredMethods()) {
			if(method.getAnnotation(Hook.class) != null){
				if(method.getParameterCount() != 1) {
					throw new IllegalArgumentException("Hook has to added to any method with exactly one argument");
				}
				result.add(new EventBridgeListener(object, method));
			}
		}
		return result;
	}

	Collection<EventBridge> createHook(Object object) {
		Keller.parameterNotNull(object);
		final List<EventBridge> result = new ArrayList<>();
		for(Method method : object.getClass().getDeclaredMethods()) {
			if(method.getAnnotation(Hook.class) != null){
				if(method.getParameterCount() != 1) {
					throw new IllegalArgumentException("Hook has to added to any method with exactly one argument");
				}
				result.add(new EventBridgeHook(object, method));
			}
		}
		return result;
	}

}
