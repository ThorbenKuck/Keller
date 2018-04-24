package com.github.thorbenkuck.keller.command;

import com.github.thorbenkuck.keller.datatypes.interfaces.GenericRunnable;
import com.github.thorbenkuck.keller.datatypes.interfaces.QueuedAction;

import java.util.Collection;

public interface Enforcer<T> extends GenericRunnable<T> {

	static <T> Enforcer<T> create() {
		return new CommandEnforcer<>();
	}

	void awaitFinish() throws InterruptedException;

	void setDoOnFinish(QueuedAction queuedAction);

	void addCommand(Command<T> command);

	void addCommand(Collection<Command<T>> commandCollection);

	boolean running();
}
