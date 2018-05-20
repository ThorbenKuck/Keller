package com.github.thorbenkuck.keller.di;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

public interface DependencyManager {

	static DependencyManager create() {
		return new NativeDependencyManager();
	}

	void applyNew(Map<Class<?>, Object> dependencies);

	void addPreConstructedDependency(Object object);

	void clear();

	<T> T removeStateDependency(Class<T> tClass);

	void addAs(Object object, Class<?> type);

	void addAsIfNotContained(Object object, Class<?> type);

	void addPreConstructedDependencyIfNotContained(Object object);

	<T> T getSetDependency(Class<T> clazz);

	boolean isSet(Class<?> clazz);

	<T> T get(Class<T> type);

	<T> T getAccordingToAnnotation(Class<T> type, Annotation[] annotation);

	List<Object> getCachedDependencies();
}
