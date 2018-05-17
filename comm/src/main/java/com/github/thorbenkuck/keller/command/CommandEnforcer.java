package com.github.thorbenkuck.keller.command;

import com.github.thorbenkuck.keller.annotations.APILevel;
import com.github.thorbenkuck.keller.datatypes.interfaces.QueuedAction;
import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.keller.pipe.Pipeline;
import com.github.thorbenkuck.keller.sync.Synchronize;
import com.github.thorbenkuck.keller.utility.Keller;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

@APILevel
final class CommandEnforcer<T> implements Enforcer<T> {

	private final Value<Boolean> running = Value.of(false);
	private final Lock lock = new ReentrantLock(false);
	private final Pipeline<T> pipeline = Pipeline.unifiedCreation();
	private final Value<QueuedAction> onFinish = Value.of(() -> {});
	private final Synchronize synchronize = Synchronize.createDefault();

	@APILevel
	CommandEnforcer() {
		// Do nothing. This Constructor
		// exists, so that an empty
		// CommandEnforcer can be created
		// (without any internal Elements)
	}

	@APILevel
	CommandEnforcer(final Collection<Command<T>> core) {
		addCommand(core);
	}

	@APILevel
	CommandEnforcer(final Command<T>... coreArray) {
		this(Arrays.asList(coreArray));
	}

	private void setUp() {
		running.set(false);
		synchronize.goOn();
		synchronize.reset();
	}

	private void afterRun() {
		if(onFinish != null) {
			QueuedAction.call(onFinish.get());
		}
	}

	@Override
	public void run(final T t) {
		try {
			lock.lock();
			running.set(true);
			setUp();
			synchronized (pipeline) {
				pipeline.apply(t);
			}
			afterRun();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			synchronize.goOn();
			lock.unlock();
			running.set(false);
		}
	}

	@Override
	public void awaitFinish() throws InterruptedException {
		synchronize.synchronize();
	}

	@Override
	public void setDoOnFinish(final QueuedAction queuedAction) {
		Keller.parameterNotNull(queuedAction);
		this.onFinish.set(queuedAction);
	}

	@Override
	public void addCommand(final Command<T> command) {
		Keller.parameterNotNull(command);
		synchronized (pipeline) {
			pipeline.addLast(new CommandWrapper<>(command));
		}
	}

	@Override
	public void addCommand(final Collection<Command<T>> commandCollection) {
		Keller.parameterNotNull(commandCollection);
		for(Command<T> command : commandCollection) {
			Keller.parameterNotNull(command);
			addCommand(command);
		}
	}

	@Override
	public boolean running() {
		return running.get();
	}

	private final class CommandWrapper<U> implements Consumer<U> {

		private final Command<U> command;

		private CommandWrapper(final Command<U> command) {
			this.command = command;
		}

		/**
		 * Performs this operation on the given argument.
		 *
		 * @param u the input argument
		 */
		@Override
		public void accept(final U u) {
			Command.run(command, u);
		}

		@Override
		public boolean equals(final Object object) {
			if (this == object) return true;
			if (object == null) return false;
			if (!(object instanceof CommandWrapper)) {
				return object instanceof Command && command.equals(object);
			}

			CommandWrapper<?> that = (CommandWrapper<?>) object;

			return command.equals(that.command);
		}

		@Override
		public int hashCode() {
			return command.hashCode();
		}
	}
}
