package com.github.thorbenkuck.keller.state.transitions;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

final class SleepingStateTransition extends NativeStateTransition {

	private final long timeOut;
	private final TimeUnit timeOutUnit;
	private final ExecutorService executorService;

	public SleepingStateTransition(long timeOut, TimeUnit timeOutUnit, ExecutorService executorService) {
		this.timeOut = timeOut;
		this.timeOutUnit = timeOutUnit;
		this.executorService = executorService;
	}

	@Override
	public void initialize() {
		super.initialize();
		executorService.execute(() -> {
			try {
				Thread.sleep(timeOutUnit.toMillis(timeOut));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			super.finish();
		});
	}

	@Override
	public void finish() {
	}
}
