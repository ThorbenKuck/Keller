package com.github.thorbenkuck.keller.state.transitions;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class TransitionThreadPool {

	private static ExecutorService executorService;

	public static ExecutorService getExecutorService() {
		checkNull();
		return executorService;
	}

	private static synchronized void checkNull() {
		if (executorService == null) {
			executorService = Executors.newCachedThreadPool();
		}
	}
}
