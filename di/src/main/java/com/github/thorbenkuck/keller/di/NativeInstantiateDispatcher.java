package com.github.thorbenkuck.keller.di;

import com.github.thorbenkuck.keller.annotations.APILevel;
import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.keller.di.annotations.Bind;
import com.github.thorbenkuck.keller.di.annotations.BindAs;
import com.github.thorbenkuck.keller.di.annotations.Cache;
import com.github.thorbenkuck.keller.di.exceptions.ConflictingBindingStatements;
import com.github.thorbenkuck.keller.di.exceptions.InstantiateException;
import com.github.thorbenkuck.keller.pipe.Pipeline;
import com.github.thorbenkuck.keller.pipe.PipelineCondition;
import com.github.thorbenkuck.keller.utility.Keller;

import java.lang.reflect.AnnotatedType;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

final class NativeInstantiateDispatcher implements InstantiateDispatcher {

	private final List<InstantiateStrategy> instantiateStrategies = new ArrayList<>();
	private final Pipeline<Object> postCreationPipeline = Pipeline.unifiedCreation();
	private final Map<Class<?>, Object> bindings = new HashMap<>();
	private final Map<Class<?>, Object> injected;
	private final Value<Function<Class<?>, Object>> defaultSupplier = Value.synchronize(new NativeDefaultConsumer());

	NativeInstantiateDispatcher() {
		this(new HashMap<>());
	}

	NativeInstantiateDispatcher(final Map<Class<?>, Object> inject) {
		this.injected = inject;
	}

	private InstantiateStrategy findFor(final Class<?> clazz) {
		final List<InstantiateStrategy> copy;
		synchronized (instantiateStrategies) {
			copy = new ArrayList<>(instantiateStrategies);
		}

		for (InstantiateStrategy instantiateStrategy : copy) {
			if (instantiateStrategy.isApplicable(clazz)) {
				return instantiateStrategy;
			}
		}

		return null;
	}

	private Object checkAndReturn(final Class<?> type, final Object result) {
		if (!type.isAssignableFrom(result.getClass())) {
			throw new InstantiateException("Something is miss configured.." + "\n"
					+ "Instantiated: " + result + "\n"
					+ "Requested:    " + type + "\n"
					+ "The instantiated object is not assignable from the requested Class");
		}

		postCreationPipeline.apply(result);

		return result;
	}

	private Object dispatchGet(final Class<?> clazz) {
		final InstantiateStrategy instantiateStrategy = findFor(clazz);
		if (instantiateStrategy == null) {
			return defaultSupplier.get().apply(clazz);
		}

		final Object t = instantiateStrategy.get(clazz, Keller.merge(injected, bindings));

		return checkAndReturn(clazz, t);
	}

	private Object dispatchCreation(Class<?> clazz) {
		final InstantiateStrategy instantiateStrategy = findFor(clazz);
		if (instantiateStrategy == null) {
			return defaultSupplier.get().apply(clazz);
		}

		final Object t = instantiateStrategy.construct(clazz, Keller.merge(injected, bindings));

		return checkAndReturn(clazz, t);
	}

	@Override
	public PipelineCondition<Object> addPostCreationConsumer(Consumer<Object> consumer) {
		synchronized (postCreationPipeline) {
			return postCreationPipeline.addFirst(consumer);
		}
	}

	@Override
	public void removePostCreationConsumer(Consumer<Object> consumer) {
		synchronized (postCreationPipeline) {
			postCreationPipeline.remove(consumer);
		}
	}

	@Override
	public void setDefaultSupplier(Function<Class<?>, Object> defaultSupplier) {
		this.defaultSupplier.set(defaultSupplier);
	}

	@Override
	public Object getBinding(Class<?> clazz) {
		synchronized (bindings) {
			return bindings.get(clazz);
		}
	}

	@Override
	public boolean isBindingSet(final Class<?> clazz) {
		synchronized (bindings) {
			synchronized (injected) {
				return bindings.get(clazz) != null && injected.get(clazz) != null;
			}
		}
	}

	@Override
	public void addBinding(final Class<?> clazz, Object object) {
		if (isBindingSet(clazz)) {
			throw new ConflictingBindingStatements("Tried to add Binding: " + clazz + "=>" + object + "\n"
					+ "This Binding has already been done!" + "\n"
					+ "Set Binding: " + clazz + "=>" + getBinding(clazz));
		}

		synchronized (bindings) {
			bindings.put(clazz, object);
		}
	}

	@Override
	public <T> T construct(final Class<T> clazz) {
		return (T) dispatchCreation(clazz);
	}

	@Override
	public <T> T getOrConstruct(final Class<T> clazz) {
		return (T) dispatchGet(clazz);
	}

	@Override
	public void addDispatchingStrategy(InstantiateStrategy strategy) {
		synchronized (instantiateStrategies) {
			instantiateStrategies.add(strategy);
		}
	}

	@Override
	public Map<Class<?>, Object> getBindings() {
		synchronized (bindings) {
			return new HashMap<>(bindings);
		}
	}

	public void clear() {
		synchronized (bindings) {
			bindings.clear();
		}
	}

	@APILevel
	private final class NativeDefaultConsumer implements Function<Class<?>, Object> {

		/**
		 * Applies this function to the given argument.
		 *
		 * @param aClass the function argument
		 * @return the function result
		 */
		@Override
		public Object apply(final Class<?> aClass) {
			throw new InstantiateException("Could not find a suitable InstantiateStrategy for " + aClass);
		}
	}
}
