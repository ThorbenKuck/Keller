package de.thorbenkuck.keller.command;

import de.thorbenkuck.keller.datatypes.interfaces.QueuedAction;
import de.thorbenkuck.keller.pipe.Pipeline;
import de.thorbenkuck.keller.pipe.QueuedPipeline;

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
			onFinish.doBefore();
			onFinish.doAction();
			onFinish.doAfter();
		}
	}

	@Override
	public void runOn(T t) {
		try {
			lock.lock();
			running = true;
			setUp();
			synchronized (pipeline) {
				pipeline.doPipeline(t);
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
