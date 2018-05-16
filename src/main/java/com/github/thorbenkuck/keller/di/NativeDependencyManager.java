package com.github.thorbenkuck.keller.di;

import com.github.thorbenkuck.keller.utility.Keller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class NativeDependencyManager implements DependencyManager {

	private final Map<Class<?>, Object> dependencies = new HashMap<>();
	private final DIConstructor diConstructor = new DIConstructor();

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

	public <T> T remove(Class<T> tClass) {
		synchronized (dependencies) {
			return (T) dependencies.remove(tClass);
		}
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

		final Map<Class<?>, Object> copy;

		synchronized (dependencies) {
			copy = new HashMap<>(dependencies);
		}

		return diConstructor.construct(type, copy, this::addPreConstructedDependencyIfNotContained);
	}

	@Override
	public List<Object> getCachedDependencies() {
		synchronized (dependencies) {
			return new ArrayList<>(dependencies.values());
		}
	}

}
