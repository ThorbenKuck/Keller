package com.github.thorbenkuck.keller.di;

import com.github.thorbenkuck.keller.datatypes.interfaces.TriConsumer;
import com.github.thorbenkuck.keller.utility.Keller;

import java.lang.annotation.Annotation;
import java.util.*;

final class NativeDependencyManager implements DependencyManager {

	private final Map<Class<?>, Object> dependencies = new HashMap<>();
	private final DIConstructor diConstructor = new DIConstructor();
	private final Map<Annotation, TriConsumer<Class, Map<Class<?>, Object>, DependencyManager>> annotationStrategies = new HashMap<>();

	NativeDependencyManager() {
		// annotationStrategies.put(RequireNew.class, (aClass, classObjectMap, dependencyManager) -> )
	}

	private <T> T dispatchConstruction(Class<T> type) {
		final Map<Class<?>, Object> copy;

		synchronized (dependencies) {
			copy = new HashMap<>(dependencies);
		}

		return diConstructor.construct(type, copy, this::addAsIfNotContained);
	}

	@Override
	public void applyNew(final Map<Class<?>, Object> dependencies) {
		for(Object object : dependencies.values()) {
			if(!isSet(object.getClass())) {
				addPreConstructedDependency(object);
			}
		}
	}

	@Override
	public void addPreConstructedDependency(final Object object) {
		Keller.parameterNotNull(object);
		synchronized (dependencies) {
			dependencies.put(object.getClass(), object);
		}
	}

	@Override
	public void clear() {
		synchronized (dependencies) {
			dependencies.clear();
		}
	}

	@Override
	public <T> T removeStateDependency(Class<T> tClass) {
		synchronized (dependencies) {
			return (T) dependencies.remove(tClass);
		}
	}

	@Override
	public void addAs(final Object object, final Class<?> type) {
		Keller.parameterNotNull(object, type);
		if (!type.isAssignableFrom(object.getClass())) {
			throw new IllegalArgumentException("Provided Class (" + type + ") is not assignable from " + object.getClass());
		}

		synchronized (dependencies) {
			dependencies.put(type, object);
		}
	}

	@Override
	public void addAsIfNotContained(final Object object, final Class<?> type) {
		Keller.parameterNotNull(object, type);
		if (isSet(type)) {
			return;
		}
		addAs(object, type);
	}

	@Override
	public void addPreConstructedDependencyIfNotContained(final Object object) {
		Keller.parameterNotNull(object);
		if(isSet(object.getClass())) {
			return;
		}
		addPreConstructedDependency(object);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getSetDependency(final Class<T> clazz) {
		synchronized (dependencies) {
			return (T) dependencies.get(clazz);
		}
	}

	@Override
	public boolean isSet(final Class<?> clazz) {
		return getSetDependency(clazz) != null;
	}

	@Override
	public <T> T get(final Class<T> type) {
		if(isSet(type)) {
			return getSetDependency(type);
		}

		return dispatchConstruction(type);
	}

	@Override
	public <T> T getAccordingToAnnotation(final Class<T> type, final Annotation[] annotations) {
		//for(Annotation annotation : com.github.thorbenkuck.keller.annotations) {
		//	TriConsumer<Class, Map<Class<?>, Object>, DependencyManager> strategy = annotationStrategies.get(annotation);
		//	if(strategy != null) {
		//		strategy.accept(type, dependencies, this);
		//	}
		//}
		if (annotations != null && Arrays.stream(annotations).anyMatch(annotation -> annotation instanceof RequireNew)) {
			return dispatchConstruction(type);
		} else {
			return get(type);
		}
	}

	@Override
	public List<Object> getCachedDependencies() {
		synchronized (dependencies) {
			return new ArrayList<>(dependencies.values());
		}
	}

}
