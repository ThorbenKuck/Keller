package com.github.thorbenkuck.keller.mvp;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

class ThreadPoolCache {

	private static ExecutorService executorService;
	private static final Semaphore semaphore = new Semaphore(1);

	static ExecutorService getExecutorService() {
		return lazyGet();
	}

	private static ExecutorService lazyGet() {
		ExecutorService returnValue;
		try {
			semaphore.acquire();

			if(executorService == null || executorService.isShutdown()) {
				executorService = Executors.newCachedThreadPool();
			}
			returnValue = executorService;
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new IllegalStateException("Creation of ExecutorService failed!");
		} finally {
			semaphore.release();
		}

		return returnValue;
	}

}
