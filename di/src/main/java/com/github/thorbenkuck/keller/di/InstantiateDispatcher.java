package com.github.thorbenkuck.keller.di;

import com.github.thorbenkuck.keller.pipe.PipelineCondition;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public interface InstantiateDispatcher {

	static InstantiateDispatcher create() {
		return new NativeInstantiateDispatcher();
	}

	void removePostCreationConsumer(Consumer<Object> consumer);

	void setDefaultSupplier(Function<Class<?>, Object> defaultSupplier);

	Object getBinding(Class<?> clazz);

	boolean isBindingSet(Class<?> clazz);

	void addBinding(Class<?> clazz, Object object);

	void addDispatchingStrategy(final InstantiateStrategy strategy);

	PipelineCondition<Object> addPostCreationConsumer(Consumer<Object> consumer);

	<T> T construct(Class<T> clazz);

	<T> T getOrConstruct(Class<T> clazz);

	Map<Class<?>, Object> getBindings();

	void clear();
}
