package com.github.thorbenkuck.keller.command;

import com.github.thorbenkuck.keller.datatypes.interfaces.QueuedAction;
import com.github.thorbenkuck.keller.pipe.Pipeline;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class CommandEnforcer<T> implements Enforcer<T> {

	private boolean running = false;
	private final Lock countDownLock = new ReentrantLock(false);
	private final Lock lock = new ReentrantLock(false);
	private final Pipeline<T> pipeline = Pipeline.unifiedCreation();
	private QueuedAction onFinish = () -> {};
	private CountDownLatch countDownLatch = new CountDownLatch(0);

	public CommandEnforcer() {
		// Do nothing. This Constructor
		// exists, so that an empty
		// CommandEnforcer can be created
		// (without any internal Elements)
	}

	public CommandEnforcer(Collection<Command<T>> core) {
		addCommand(core);
	}

	public CommandEnforcer(Command<T>... coreArray) {
		this(Arrays.asList(coreArray));
	}

	private void overrideExistingCountDownLatch() {
		try {
			countDownLock.lock();
			while (countDownLatch.getCount() > 0) {
				countDownLatch.countDown();
			}
		} finally {
			countDownLock.unlock();
		}
	}

	private void setUp() {
		running = true;
		if (countDownLatch.getCount() > 0) {
			overrideExistingCountDownLatch();
		}
		countDownLatch = new CountDownLatch(1);
	}

	private void afterRun() {
		if(onFinish != null) {
			QueuedAction.call(onFinish);
		}
	}

	@Override
	public void run(T t) {
		try {
			lock.lock();
			running = true;
			setUp();
			synchronized (pipeline) {
				pipeline.apply(t);
			}
			afterRun();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			countDownLatch.countDown();
			lock.unlock();
			running = false;
		}
	}

	@Override
	public void awaitFinish() throws InterruptedException {
		countDownLatch.await();
	}

	@Override
	public void setDoOnFinish(QueuedAction queuedAction) {
		if(queuedAction == null) {
			throw new IllegalArgumentException("QueuedAction provided cannot be null!");
		}
		this.onFinish = queuedAction;
	}

	@Override
	public void addCommand(Command<T> command) {
		synchronized (pipeline) {
			pipeline.addLast(new CommandWrapper<>(command));
		}
	}

	@Override
	public void addCommand(Collection<Command<T>> commandCollection) {
		for(Command<T> command : commandCollection) {
			addCommand(command);
		}
	}

	@Override
	public boolean running() {
		return running;
	}

	private final class CommandWrapper<U> implements Consumer<U> {

		private final Command<U> command;

		private CommandWrapper(Command<U> command) {
			this.command = command;
		}

		/**
		 * Performs this operation on the given argument.
		 *
		 * @param u the input argument
		 */
		@Override
		public void accept(U u) {
			command.execute(u);
			command.afterExecution();
		}

		@Override
		public boolean equals(Object object) {
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
