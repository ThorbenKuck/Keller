package com.github.thorbenkuck.keller.command;

@FunctionalInterface
public interface Command<T> {

	void execute(T t);

	default void afterExecution() {}

	static <T> void run(Command<T> command, T t) {
		command.execute(t);
		command.afterExecution();
	}
}
