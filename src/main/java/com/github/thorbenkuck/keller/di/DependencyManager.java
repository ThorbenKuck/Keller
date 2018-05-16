package com.github.thorbenkuck.keller.di;

import java.util.List;
import java.util.Map;

public interface DependencyManager {

	static DependencyManager create() {
		return new NativeDependencyManager();
	}

	void applyNew(Map<Class<?>, Object> dependencies);

	void addPreConstructedDependency(Object object);

	void addPreConstructedDependencyIfNotContained(Object object);

	<T> T getSetDependency(Class<T> clazz);

	boolean isSet(Class<?> clazz);

	<T> T get(Class<T> type);

	List<Object> getCachedDependencies();
}
