package com.github.thorbenkuck.keller.di;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.keller.di.annotations.RequireNew;
import com.github.thorbenkuck.keller.di.annotations.Use;
import com.github.thorbenkuck.keller.di.exceptions.InstantiateException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

final class DIConstructor {

	private final DependencyManager dispatcher;

	DIConstructor(DependencyManager dispatcher) {
		this.dispatcher = dispatcher;
	}

	DIConstructor() {
		dispatcher = null;
	}

	private <T> T instantiateCheckAndReturn(final Constructor<?> constructor, final Object[] parameters) {
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

			return (T) instance;
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new InstantiateException(e);
		}
	}

	private Object[] constructParameters(final Constructor<?> constructor, final Map<Class<?>, Object> constructed) {
		if (constructor == null) {
			return new Object[0];
		}
		final Class<?>[] types = constructor.getParameterTypes();
		final Object[] parameters = new Object[types.length];
		final Annotation[][] annotations = constructor.getParameterAnnotations();

		for (int i = 1; i < parameters.length; i++) {
			final Class<?> clazz = types[i];
			final Object instance = createSingleParameter(clazz, constructed, annotations[i -1]);

			if (instance == null) {
				throw new InstantiateException("Could not construct parameter " + clazz);
			}
			parameters[i] = instance;
		}

		return parameters;
	}

	private Object dispatchCreation(final Class<?> clazz, final Map<Class<?>, Object> constructed) {
		if(dispatcher != null) {
			return dispatcher.get(clazz);
		}

		return construct(clazz, constructed);
	}

	private Object createSingleParameter(final Class<?> clazz, final Map<Class<?>, Object> constructed, final Annotation[] annotations) {
		final Object instance;
		boolean enforce = false;
		for(Annotation annotation : annotations) {
			if (annotation instanceof RequireNew) {
				enforce = true;
			}
		}

		if (enforce || constructed.get(clazz) == null) {
			instance = dispatchCreation(clazz, constructed);
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
					throw new InstantiateException("The Class " + clazz + " has multiple Constructors annotated with @Use.");
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
			throw new InstantiateException("The Class " + clazz + " cannot be constructed!\n" +
					"It has to provide any Constructor without parameters, a Constructor annotated with @Use or exactly one Constructor");
		}
	}

	final <T> T construct(final Class<T> clazz, final Map<Class<?>, Object> preConstructedDependencies) {
		final Map<Class<?>, Object> constructed = new HashMap<>(preConstructedDependencies);
		final Constructor<?> constructor = getConstructor(clazz);

		return instantiateCheckAndReturn(constructor, constructParameters(constructor, constructed));
	}
}
