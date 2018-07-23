package com.github.thorbenkuck.keller.event.eventbus.exceptions;

public final class DeadEventException extends RuntimeException {

	public DeadEventException() {
	}

	public DeadEventException(String message) {
		super(message);
	}

	public DeadEventException(String message, Throwable cause) {
		super(message, cause);
	}

	public DeadEventException(Throwable cause) {
		super(cause);
	}

	public DeadEventException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
