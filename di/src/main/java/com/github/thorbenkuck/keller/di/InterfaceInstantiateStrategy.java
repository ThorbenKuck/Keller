package com.github.thorbenkuck.keller.di;

import com.github.thorbenkuck.keller.di.annotations.Implementation;
import com.github.thorbenkuck.keller.di.exceptions.InstantiateException;
import com.github.thorbenkuck.keller.utility.Keller;

import java.util.Map;

final class InterfaceInstantiateStrategy implements InstantiateStrategy {

	private final DIConstructor diConstructor;

	InterfaceInstantiateStrategy(DIConstructor diConstructor) {
		this.diConstructor = diConstructor;
	}

	private <T> T dispatchConstruct(final Class<T> type, final Map<Class<?>, Object> bindings) {
		T result = diConstructor.construct(type, bindings);

		if (result.getClass().isAssignableFrom(type)) {
			return result;
		}

		throw new InstantiateException("Could not correctly construct ");
	}

	@Override
	public <T> T construct(final Class<T> type, final Map<Class<?>, Object> bindings) {
		if (!type.isInterface()) {
			throw new IllegalArgumentException("Expected an Interface to be instantiated. Provided: " + type);
		}

		if (type.isAnnotationPresent(Implementation.class)) {
			final Class<?> implementation = type.getAnnotation(Implementation.class).value();
			return (T) dispatchConstruct(implementation, bindings);
		}

		throw new InstantiateException("The requested Class " + type + " could not be constructed." + "\n"
				+ "For creating new interface, make sure you provide the @Implementation, or to inject an instance with the @Bind annotation.");
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(final Class<T> type, final Map<Class<?>, Object> bindings) {
		if (!type.isInterface()) {
			throw new IllegalArgumentException("Expected an Interface to be instantiated. Provided: " + type);
		}

		if (type.isAnnotationPresent(Implementation.class)) {
			final Class<?> implementation = type.getAnnotation(Implementation.class).value();
			final Object binding = bindings.get(implementation);
			if (binding != null && binding.getClass().isAssignableFrom(type)) {
				return (T) binding;
			} else {
				return (T) dispatchConstruct(implementation, bindings);
			}
		}

		final Object potentialBinding = bindings.get(type);
		if(potentialBinding != null && potentialBinding.getClass().isAssignableFrom(type)) {
			return (T) bindings.get(type);
		}

		return construct(type, bindings);
	}

	@Override
	public boolean isApplicable(Class<?> clazz) {
		return !Keller.isPrimitiveOrWrapperType(clazz) && clazz.isInterface();
	}
}
