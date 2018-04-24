package com.github.thorbenkuck.keller.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class Listening {

	private final Object object;
	private final Method method;
	private final Class<?> parameterType;

	Listening(Object object, Method method) {
		if(method.getParameterCount() != 1) {
			throw new IllegalArgumentException();
		}
		this.object = object;
		this.method = method;
		parameterType = method.getParameterTypes()[0];
	}

	Object trigger(Object event) {
		boolean access = method.isAccessible();
		method.setAccessible(true);
		Object result = null;
		try {
			if (parameterType.equals(event.getClass())) {
				try {
					result = method.invoke(object, event);
				} catch (IllegalAccessException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		} finally {
			method.setAccessible(access);
		}

		return result;
	}

	boolean isApplicable(Object event) {
		return event != null && parameterType.equals(event.getClass());
	}

	Class<?> getEventType() {
		return parameterType;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Listening listening = (Listening) o;

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
