package com.github.thorbenkuck.keller.di;

import com.github.thorbenkuck.keller.utility.Keller;

import java.lang.reflect.Modifier;
import java.util.Map;

final class ClassInstantiateStrategy implements InstantiateStrategy {

	private final DIConstructor constructor;

	ClassInstantiateStrategy(DIConstructor constructor) {
		this.constructor = constructor;
	}

	@Override
	public <T> T construct(Class<T> type, Map<Class<?>, Object> bindings) {
		return constructor.construct(type, bindings);
	}

	@Override
	public boolean isApplicable(Class<?> clazz) {
		return !Keller.isPrimitiveOrWrapperType(clazz) && !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers());
	}
}
