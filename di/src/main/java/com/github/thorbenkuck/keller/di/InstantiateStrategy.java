package com.github.thorbenkuck.keller.di;

import java.util.Map;

public interface InstantiateStrategy {

	<T> T construct(final Class<T> type, final Map<Class<?>, Object> bindings);

	default <T> T get(final Class<T> type, final Map<Class<?>, Object> bindings) {
		if(bindings.get(type) != null) {
			return (T) bindings.get(type);
		}

		return construct(type, bindings);
	}

	boolean isApplicable(final Class<?> clazz);

}
