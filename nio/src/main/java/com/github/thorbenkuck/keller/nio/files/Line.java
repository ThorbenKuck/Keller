package com.github.thorbenkuck.keller.nio.files;

import com.github.thorbenkuck.keller.datatypes.interfaces.PrettyPrint;

public interface Line extends PrettyPrint {

	static Line create(int number, String content) {
		return new NativeLine(number, content);
	}

	int getLineNumber();

	String getContent();

}
