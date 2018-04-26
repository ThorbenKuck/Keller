package com.github.thorbenkuck.keller.nio.files;

final class NativeLine implements Line {

	private final int lineNumber;
	private final String content;

	NativeLine(int lineNumber, String content) {
		this.lineNumber = lineNumber;
		this.content = content;
	}

	@Override
	public final int getLineNumber() {
		return lineNumber;
	}

	@Override
	public final String getContent() {
		return content;
	}

	@Override
	public final String toString() {
		return getContent();
	}

	@Override
	public final String toReadable() {
		return "(" + getLineNumber() + "): " + getContent();
	}
}
