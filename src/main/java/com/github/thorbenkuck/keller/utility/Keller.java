package com.github.thorbenkuck.keller.utility;

import java.util.Objects;

public class Keller {

	public static boolean isNull(Object o) {
		return Objects.isNull(o);
	}

	public static void requireNotNull(Object o) {
		Objects.requireNonNull(o);
	}

	public static void requireNotNull(Object... objects) {
		for(Object o : objects){
			requireNotNull(o);
		}
	}

	public static void parameterNotNull(Object o) {
		if(isNull(o)) {
			throw new IllegalArgumentException("Null is not allowed as parameter");
		}
	}

	public static void parameterNotNull(Object... objects) {
		for(Object o : objects) {
			parameterNotNull(o);
		}
	}
}
