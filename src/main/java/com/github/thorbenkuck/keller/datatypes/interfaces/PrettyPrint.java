package com.github.thorbenkuck.keller.datatypes.interfaces;

import java.io.PrintStream;

public interface PrettyPrint {

	default void prettyPrint(PrintStream writer) {
		writer.println(prettyPrint());

	}

	String prettyPrint();

}
