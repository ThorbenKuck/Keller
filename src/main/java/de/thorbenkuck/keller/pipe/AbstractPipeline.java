package de.thorbenkuck.keller.pipe;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractPipeline<T, C extends Collection<PipelineElement<T>>> implements Pipeline<T>, Serializable {

	private final C core;
	private CountDownLatch countDownLatch = new CountDownLatch(0);

	protected AbstractPipeline(C c) {
		this.core = c;
	}

	@Override
	public void run(T element) {
		try {
			assertIsOpen();
			lock();
			core.forEach(pipelineService -> pipelineService.run(element));
		} finally {
			unlock();
		}

	}

	@Override
	public void lock() {
		synchronized (core) {
			try {
				countDownLatch.await();
				countDownLatch = new CountDownLatch(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void unlock() {
		synchronized (core) {
			countDownLatch.countDown();
		}
	}

	protected C getCore() {
		return this.core;
	}
}
