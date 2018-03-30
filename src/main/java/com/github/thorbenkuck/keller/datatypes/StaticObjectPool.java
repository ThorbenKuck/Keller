package com.github.thorbenkuck.keller.datatypes;

import com.github.thorbenkuck.keller.utility.Keller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public final class StaticObjectPool {

	private static final Map<Class<?>, Object> internals = new HashMap<>();

	private StaticObjectPool() {
		throw new UnsupportedOperationException("This is not the class you are looking for!");
	}

	public static boolean isSet(Class clazz) {
		synchronized (internals) {
			return internals.get(clazz) != null;
		}
	}

	public static <T> T access(Class<T> tClass) {
		Keller.parameterNotNull(tClass);
		Object object;
		synchronized (internals) {
			object = internals.get(tClass);
		}
		return (T) object;
	}

	public static <T> Optional<T> getInstance(Class<T> tClass) {
		return Optional.ofNullable(access(tClass));
	}

	public static void setInstance(Object object) {
		Keller.parameterNotNull(object);

		Class clazz = object.getClass();
		synchronized (internals) {
			internals.put(clazz, object);
		}
	}

	public static void setIfNotContained(Object object) {
		Keller.parameterNotNull(object);

		if(isSet(object.getClass())) {
			return;
		}
		setInstance(object);
	}

	public static <T> void ifSet(Class<T> clazz, Consumer<T> consumer) {
		if(!isSet(clazz)) {
			// This little check is
			// done, so that we can
			// return before constructing
			// any new object, or
			// calling any other method.
			return;
		}
		getInstance(clazz).ifPresent(consumer);
	}
}
