package de.thorbenkuck.keller.command;

import de.thorbenkuck.keller.datatypes.interfaces.QueuedAction;

import java.util.Collection;

public interface Enforcer<T> {
	void runOn(T t);

	void awaitFinish() throws InterruptedException;

	void setDoOnFinish(QueuedAction queuedAction);

	void addCommand(Command<T> command);

	void addCommand(Collection<Command<T>> commandCollection);

	boolean running();
}
