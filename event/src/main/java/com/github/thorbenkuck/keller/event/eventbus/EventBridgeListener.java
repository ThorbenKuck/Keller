package com.github.thorbenkuck.keller.event.eventbus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class EventBridgeListener implements EventBridge {

	private final Object object;
	private final Method method;
	private final Class<?> parameterType;

	EventBridgeListener(final Object object, final Method method) {
		if(method.getParameterCount() != 1) {
			throw new IllegalArgumentException();
		}
		this.object = object;
		this.method = method;
		parameterType = method.getParameterTypes()[0];
	}

	@Override
	public Object trigger(final Object event) {
		boolean access = method.isAccessible();
		method.setAccessible(true);
		try {
			if (parameterType.equals(event.getClass())) {
				try {
					method.invoke(object, event);
				} catch (final IllegalAccessException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		} finally {
			method.setAccessible(access);
		}

		return null;
	}

	@Override
	public Object getSource() {
		return object;
	}

	@Override
	public boolean isApplicable(final Object event) {
		return event != null && parameterType.equals(event.getClass());
	}

	@Override
	public Class<?> getEventType() {
		return parameterType;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final EventBridgeListener listening = (EventBridgeListener) o;

		return object.equals(listening.object) && method.equals(listening.method) && parameterType.equals(listening.parameterType);
	}

	@Override
	public int hashCode() {
		int result = object.hashCode();
		result = 31 * result + method.hashCode();
		result = 31 * result + parameterType.hashCode();
		return result;
	}
}
