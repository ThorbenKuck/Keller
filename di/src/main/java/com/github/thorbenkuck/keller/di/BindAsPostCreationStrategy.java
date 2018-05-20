package com.github.thorbenkuck.keller.di;

import com.github.thorbenkuck.keller.di.annotations.BindAs;

final class BindAsPostCreationStrategy implements PostCreationStrategy {

	private final InstantiateDispatcher dispatcher;

	BindAsPostCreationStrategy(InstantiateDispatcher dispatcher) {
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
		if (clazz.isAnnotationPresent(BindAs.class)) {
			final BindAs bindAs = clazz.getAnnotation(BindAs.class);
			final Class<?>[] bindingTypes = bindAs.value();
			for (Class<?> type : bindingTypes) {
				dispatcher.addBinding(type, o);
			}
		}
	}
}
