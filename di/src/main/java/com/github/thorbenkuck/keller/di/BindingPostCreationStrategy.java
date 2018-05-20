package com.github.thorbenkuck.keller.di;

import com.github.thorbenkuck.keller.di.annotations.Bind;

import java.lang.reflect.AnnotatedType;

final class BindingPostCreationStrategy implements PostCreationStrategy {

	private final InstantiateDispatcher dispatcher;

	BindingPostCreationStrategy(InstantiateDispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

	private void checkAnnotatedTypes(AnnotatedType[] annotatedTypes, Object o) {
		for (AnnotatedType interfaceType : annotatedTypes) {
			if (interfaceType.isAnnotationPresent(Bind.class)) {
				final Class<?> type = (Class<?>) interfaceType.getType();
				if(!dispatcher.isBindingSet(type) || !dispatcher.getBinding(type).getClass().equals(o.getClass())) {
					dispatcher.addBinding((Class<?>) interfaceType.getType(), o);
				}
			}
		}
	}

	private void checkInterface(final Class<?> clazz, final Object o) {
		final AnnotatedType[] interfaceTypes = clazz.getAnnotatedInterfaces();
		checkAnnotatedTypes(interfaceTypes, o);
	}

	private void checkSuperClass(final Class<?> clazz, final Object o) {
		final AnnotatedType superclass = clazz.getAnnotatedSuperclass();
		checkAnnotatedTypes(new AnnotatedType[]{superclass}, o);
	}

	/**
	 * Performs this operation on the given argument.
	 *
	 * @param o the input argument
	 */
	@Override
	public void accept(Object o) {
		final Class<?> clazz = o.getClass();
		checkInterface(clazz, o);
		checkSuperClass(clazz, o);
	}
}
