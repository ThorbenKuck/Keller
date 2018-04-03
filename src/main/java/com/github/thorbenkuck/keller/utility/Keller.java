package com.github.thorbenkuck.keller.utility;

import java.util.Objects;

public final class Keller {

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
