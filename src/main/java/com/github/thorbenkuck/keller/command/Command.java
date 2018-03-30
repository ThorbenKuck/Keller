package com.github.thorbenkuck.keller.command;

@FunctionalInterface
public interface Command<T> {

	void execute(T t);

	default void afterExecution() {}
}
