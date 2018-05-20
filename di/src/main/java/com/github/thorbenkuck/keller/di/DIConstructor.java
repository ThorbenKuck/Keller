package com.github.thorbenkuck.keller.di;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

final class DIConstructor {

	private Class<?>[] getBinds(Class<?> type) {
		if (type.isAnnotationPresent(BindAs.class)) {
			return type.getAnnotation(BindAs.class).value();
		} else {
			return new Class<?>[]{type};
		}
	}

	private <T> T instantiateCheckAndReturn(final Constructor<?> constructor, final Class<T> clazz,
											final BiConsumer<Object, Class<?>> constructedCallback, final Object[] parameters) {
		try {
			final Object instance;
			final boolean constructorAccessible = constructor.isAccessible();
			constructor.setAccessible(true);
			try {
				if (parameters.length == 0) {
					instance = constructor.newInstance();
				} else {
					instance = constructor.newInstance(parameters);
				}
			} finally {
				constructor.setAccessible(constructorAccessible);
			}

			final Class<?>[] types = getBinds(clazz);

			if (clazz.isAnnotationPresent(SingleInstanceOnly.class)) {
				Arrays.stream(types)
						.forEachOrdered(current -> constructedCallback.accept(instance, current));
			}

			return (T) instance;
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new IllegalStateException(e);
		}
	}

	private Object[] constructParameters(final Constructor<?> constructor, final Map<Class<?>, Object> constructed, final BiConsumer<Object, Class<?>> constructedCallback) {
		if (constructor == null) {
			return new Object[0];
		}
		final Class<?>[] types = constructor.getParameterTypes();
		final Object[] parameters = new Object[types.length];
		final Annotation[][] annotations = constructor.getParameterAnnotations();

		for (int i = 1; i < parameters.length; i++) {
			final Class<?> clazz = types[i];
			final Object instance = createSingleParameter(clazz, constructed, annotations[i -1], constructedCallback);

			if (instance == null) {
				throw new IllegalStateException("Could not construct parameter " + clazz);
			}
			parameters[i] = instance;
		}

		return parameters;
	}

	private Object createSingleParameter(final Class<?> clazz, final Map<Class<?>, Object> constructed, final Annotation[] annotations, final BiConsumer<Object, Class<?>> constructedCallback) {
		final Object instance;
		boolean enforce = false;
		for(Annotation annotation : annotations) {
			if (annotation instanceof RequireNew) {
				enforce = true;
			}
		}

		if (enforce || constructed.get(clazz) == null) {
			instance = construct(clazz, constructed, constructedCallback);
		} else {
			instance = constructed.get(clazz);
		}

		return instance;
	}

	private Constructor<?> getConstructor(final Class<?> clazz) {
		final Constructor<?>[] constructors = clazz.getConstructors();

		if (constructors.length == 1) {
			return constructors[0];
		}

		final Value<Constructor<?>> currentUsed = Value.empty();
		final Value<Constructor<?>> defaultConstructor = Value.empty();

		for (final Constructor constructor : constructors) {
			if (constructor.isAnnotationPresent(Use.class)) {
				if (!currentUsed.isEmpty()) {
					throw new IllegalStateException("The Class " + clazz + " has multiple Constructors annotated with @Use.");
				}
				currentUsed.set(constructor);
			}
			if (constructor.getParameterCount() == 0) {
				defaultConstructor.set(constructor);
			}
		}

		if (!currentUsed.isEmpty()) {
			return currentUsed.get();
		} else if (!defaultConstructor.isEmpty()) {
			return defaultConstructor.get();
		} else {
			throw new IllegalStateException("The Class " + clazz + " cannot be constructed!\n" +
					"It has to provide any Constructor without parameters or an Constructor annotated with @Use");
		}
	}

	@SuppressWarnings("unchecked")
	final <T> T construct(final Class<T> clazz, final Map<Class<?>, Object> preConstructedDependencies, final BiConsumer<Object, Class<?>> constructedCallback) {
		final Map<Class<?>, Object> constructed = new HashMap<>(preConstructedDependencies);
		final Constructor<?> constructor = getConstructor(clazz);

		return instantiateCheckAndReturn(constructor, clazz, constructedCallback, constructParameters(constructor, constructed, constructedCallback));
	}
}
