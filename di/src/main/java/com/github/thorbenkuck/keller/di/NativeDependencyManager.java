package com.github.thorbenkuck.keller.di;

import com.github.thorbenkuck.keller.annotations.APILevel;
import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.keller.di.annotations.Bind;
import com.github.thorbenkuck.keller.di.annotations.BindAs;
import com.github.thorbenkuck.keller.di.annotations.RequireNew;
import com.github.thorbenkuck.keller.utility.Keller;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.util.*;
import java.util.function.Function;

final class NativeDependencyManager implements DependencyManager {

	private final Map<Class<?>, Object> injectedDependencies = new HashMap<>();
	private final InstantiateDispatcher dispatcher = new NativeInstantiateDispatcher(injectedDependencies);
	private final Value<Function<Class<?>, Class<?>[]>> bindingTypeStrategyValue = Value.synchronize(new NativeBindingTypeStrategy());

	private <T> T dispatchConstruction(Class<T> type) {
		return dispatcher.construct(type);
	}

	private <T> T dispatchGet(Class<T> type) {
		return dispatcher.getOrConstruct(type);
	}

	private Class<?>[] getBindingType(Class<?> clazz) {
		return bindingTypeStrategyValue.get().apply(clazz);
	}

	private void dispatchInject(Class<?> clazz, Object object) {
		synchronized (injectedDependencies) {
			injectedDependencies.put(clazz, object);
		}
	}

	@Override
	public InstantiateDispatcher getDispatcher() {
		return dispatcher;
	}

	@Override
	public void applyNew(final Map<Class<?>, Object> dependencies) {
		for (Object object : dependencies.values()) {
			if (!isInjected(object.getClass())) {
				inject(object);
			}
		}
	}

	@Override
	public void clear() {
		synchronized (injectedDependencies) {
			injectedDependencies.clear();
		}

		dispatcher.clear();
	}

	@Override
	public void setBindingTypeStrategy(Function<Class<?>, Class<?>[]> strategy) {
		bindingTypeStrategyValue.set(strategy);
	}

	@Override
	public <T> T removeInjectedDependency(Class<T> tClass) {
		synchronized (injectedDependencies) {
			return (T) injectedDependencies.remove(tClass);
		}
	}

	@Override
	public void injectAsIfNotContained(final Object object, final Class<?> type) {
		Keller.parameterNotNull(object, type);
		if (isInjected(type)) {
			return;
		}
		injectAs(object, type);
	}

	@Override
	public void injectAs(final Object object, final Class<?> type) {
		Keller.parameterNotNull(object, type);
		if (!type.isAssignableFrom(object.getClass())) {
			throw new IllegalArgumentException("Provided Class (" + type + ") is not assignable from " + object.getClass());
		}

		synchronized (injectedDependencies) {
			injectedDependencies.put(type, object);
		}
	}

	@Override
	public void injectIfNotContained(final Object object) {
		Keller.parameterNotNull(object);

		for(Class<?> clazz : getBindingType(object.getClass())) {
			if (!isInjected(clazz)) {
				dispatchInject(clazz, object);
			}
		}
	}

	@Override
	public void inject(final Object object) {
		Keller.parameterNotNull(object);
		for(Class<?> clazz : getBindingType(object.getClass())) {
			dispatchInject(clazz, object);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getInjectedDependency(final Class<T> clazz) {
		synchronized (injectedDependencies) {
			return (T) injectedDependencies.get(clazz);
		}
	}

	@Override
	public boolean isInjected(final Class<?> clazz) {
		return getInjectedDependency(clazz) != null;
	}

	@Override
	public <T> T get(final Class<T> type) {
		if (isInjected(type)) {
			return getInjectedDependency(type);
		}

		return dispatchGet(type);
	}

	@Override
	public <T> T getAccordingToAnnotation(final Class<T> type, final Annotation[] annotations) {
		if (annotations != null && Arrays.stream(annotations).anyMatch(annotation -> annotation instanceof RequireNew)) {
			return dispatchConstruction(type);
		} else {
			return get(type);
		}
	}

	@Override
	public Map<Class<?>, Object> getBindings() {
		synchronized (injectedDependencies) {
			return Keller.merge(injectedDependencies, dispatcher.getBindings());
		}
	}

	@Override
	public Map<Class<?>, Object> getInjectedBinding() {
		synchronized (injectedDependencies) {
			return new HashMap<>(injectedDependencies);
		}
	}

	@Override
	public Map<Class<?>, Object> getConstructedBindings() {
		return dispatcher.getBindings();
	}

	@Override
	public List<Object> getCachedDependencies() {
		return new ArrayList<>(getConstructedBindings().values());
	}

	@Override
	public List<Object> getInjectedDependencies() {
		synchronized (injectedDependencies) {
			return new ArrayList<>(injectedDependencies.values());
		}
	}

	@APILevel
	private final class NativeBindingTypeStrategy implements Function<Class<?>, Class<?>[]> {

		/**
		 * Applies this function to the given argument.
		 *
		 * @param clazz the function argument
		 * @return the function result
		 */
		@Override
		public Class<?>[] apply(Class<?> clazz) {
			if(clazz.isAnnotationPresent(BindAs.class)) {
				return clazz.getAnnotation(BindAs.class).value();
			}

			final List<Class<?>> types = new ArrayList<>();
			if(clazz.isAnnotationPresent(Bind.class)) {
				types.add(clazz);
			}

			for(AnnotatedType annotatedType : clazz.getAnnotatedInterfaces()) {
				if(annotatedType.isAnnotationPresent(Bind.class)) {
					types.add((Class<?>) annotatedType.getType());
				}
			}

			AnnotatedType superClass = clazz.getAnnotatedSuperclass();
			if(superClass.isAnnotationPresent(Bind.class)) {
				types.add((Class<?>) superClass.getType());
			}

			if(types.isEmpty()) {
				return new Class<?>[]{clazz};
			}

			return types.toArray(new Class[types.size()]);
		}
	}
}
