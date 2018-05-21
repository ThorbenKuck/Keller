package com.github.thorbenkuck.keller.event;

public final class DeadEventException extends RuntimeException {

	DeadEventException() {
	}

	DeadEventException(String message) {
		super(message);
	}

	DeadEventException(String message, Throwable cause) {
		super(message, cause);
	}

	DeadEventException(Throwable cause) {
		super(cause);
	}

	DeadEventException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
