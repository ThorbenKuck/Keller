package com.github.thorbenkuck.keller.mvp;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

class ThreadPoolCache {

	private static ExecutorService executorService;
	private static final Semaphore semaphore = new Semaphore(1);

	static ExecutorService getExecutorService() {
		tryCreate();
		return executorService;
	}

	private static void tryCreate() {
		try {
			semaphore.acquire();

			if(executorService == null || executorService.isShutdown()) {
				executorService = Executors.newCachedThreadPool();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			semaphore.release();
		}
	}

}
