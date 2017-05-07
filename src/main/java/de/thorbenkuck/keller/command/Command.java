package de.thorbenkuck.keller.command;

@FunctionalInterface
public interface Command<T> {

	void execute(T t);

	default void afterExecute() {}
}
