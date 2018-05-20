package com.github.thorbenkuck.keller.di;

import com.github.thorbenkuck.keller.di.annotations.Cache;

final class CachePostCreationStrategy implements PostCreationStrategy {

	private final InstantiateDispatcher dispatcher;

	CachePostCreationStrategy(InstantiateDispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

	/**
	 * Performs this operation on the given argument.
	 *
	 * @param o the input argument
	 */
	@Override
	public void accept(Object o) {
		final Class<?> clazz = o.getClass();
		if (clazz.isAnnotationPresent(Cache.class)) {
			if(!dispatcher.isBindingSet(o.getClass())) {
				dispatcher.addBinding(o.getClass(), o);
			}
		}
	}
}
