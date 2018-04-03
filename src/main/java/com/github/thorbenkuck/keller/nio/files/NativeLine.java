package com.github.thorbenkuck.keller.nio.files;

class NativeLine implements Line {

	private final int lineNumber;
	private final String content;

	NativeLine(int lineNumber, String content) {
		this.lineNumber = lineNumber;
		this.content = content;
	}

	@Override
	public int getLineNumber() {
		return lineNumber;
	}

	@Override
	public String getContent() {
		return content;
	}

	@Override
	public String toString() {
		return getContent();
	}

	@Override
	public String prettyPrint() {
		return "(" + getLineNumber() + "): " + getContent();
	}
}
