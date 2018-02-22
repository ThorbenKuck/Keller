package com.github.thorbenkuck.keller.command;

import com.github.thorbenkuck.keller.datatypes.interfaces.QueuedAction;
import com.github.thorbenkuck.keller.pipe.Pipeline;
import com.github.thorbenkuck.keller.datatypes.QueuedPipeline;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CommandEnforcer<T> implements Enforcer<T> {

	private boolean running = false;
	private final Lock countDownLock = new ReentrantLock(false);
	private final Lock lock = new ReentrantLock(false);
	private final Pipeline<T> pipeline = new QueuedPipeline<>();
	private QueuedAction onFinish = () -> {};
	private CountDownLatch countDownLatch = new CountDownLatch(0);

	public CommandEnforcer() {
		// Do nothing
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
				pipeline.run(t);
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
			pipeline.addLast(command::execute);
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
}
