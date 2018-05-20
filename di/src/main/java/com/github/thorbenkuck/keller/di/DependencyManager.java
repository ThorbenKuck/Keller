package com.github.thorbenkuck.keller.di;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface DependencyManager {

	static DependencyManager create() {
		final DependencyManager dependencyManager = new NativeDependencyManager();
		final DIConstructor constructor = new DIConstructor(dependencyManager);

		final InstantiateDispatcher dispatcher = dependencyManager.getDispatcher();
		PostCreationStrategy.applyTo(dispatcher);

		dispatcher.addDispatchingStrategy(new InterfaceInstantiateStrategy(constructor));
		dispatcher.addDispatchingStrategy(new ClassInstantiateStrategy(constructor));

		return dependencyManager;
	}

	void setBindingTypeStrategy(Function<Class<?>, Class<?>[]> strategy);

	InstantiateDispatcher getDispatcher();

	void applyNew(Map<Class<?>, Object> dependencies);

	void clear();

	<T> T removeInjectedDependency(Class<T> tClass);

	void injectAsIfNotContained(Object object, Class<?> type);

	void injectAs(Object object, Class<?> type);

	void injectIfNotContained(Object object);

	void inject(Object object);

	<T> T getInjectedDependency(Class<T> clazz);

	boolean isInjected(Class<?> clazz);

	<T> T get(Class<T> type);

	<T> T getAccordingToAnnotation(Class<T> type, Annotation[] annotation);

	Map<Class<?>, Object> getBindings();

	Map<Class<?>, Object> getInjectedBinding();

	Map<Class<?>, Object> getConstructedBindings();

	List<Object> getCachedDependencies();

	List<Object> getInjectedDependencies();
}
