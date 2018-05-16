package com.github.thorbenkuck.keller.utility;

import java.util.*;

public final class Keller {

	private static final Set<Class<?>> PRIMITIVE_SETS = new HashSet<>();

	public static boolean isPrimitiveOrWrapperType(Object object) {
		return isPrimitive(object.getClass()) || isWrapper(object.getClass());
	}

	public static boolean isPrimitive(Object object) {
		return object != null && object.getClass().isPrimitive();
	}

	public static boolean isWrapper(Object object) {
		if(object == null) {
			return false;
		}
		if(PRIMITIVE_SETS.isEmpty()) {
			synchronized (Keller.class) {
				// Extra check, for catching the case,
				// that any other thread acquired the
				// access, but our isEmpty check
				// returned true beforehand.
				// This may slow down the creation
				// but prevents the overhead of trying
				// to add the same Set 2 times in a row.
				if (PRIMITIVE_SETS.isEmpty()) {
					PRIMITIVE_SETS.addAll(createWrapperTypes());
				}
			}
		}

		return PRIMITIVE_SETS.contains(object.getClass());
	}

	private static List<Class<?>> createWrapperTypes() {
		List<Class<?>> value = new ArrayList<>();
		value.add(Boolean.class);
		value.add(Character.class);
		value.add(Byte.class);
		value.add(Short.class);
		value.add(Integer.class);
		value.add(Long.class);
		value.add(Float.class);
		value.add(Double.class);
		value.add(Void.class);
		return value;
	}

	public static boolean isNull(final Object o) {
		return Objects.isNull(o);
	}

	public static void requireNotNull(final Object o) {
		Objects.requireNonNull(o);
	}

	public static void requireNotNull(final Object... objects) {
		for(final Object o : objects){
			requireNotNull(o);
		}
	}

	public static void parameterNotNull(final Object o) {
		if(isNull(o)) {
			throw new IllegalArgumentException("Null is not allowed as parameter");
		}
	}

	public static void parameterNotNull(final Object... objects) {
		for(final Object o : objects) {
			parameterNotNull(o);
		}
	}
}
