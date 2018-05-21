package com.github.thorbenkuck.keller.state.transitions;

import com.github.thorbenkuck.keller.sync.Synchronize;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public interface StateTransition {

	static StateTransition dead() {
		return new EmptyStateTransition();
	}

	static StateTransition open() {
		return new NativeStateTransition();
	}

	static StateTransition openFor(Object followingState) {
		return new NativeStateTransition(followingState);
	}

	static StateTransition hook(Synchronize synchronize) {
		return new NativeFixedStateTransition(synchronize, null);
	}

	static StateTransition hookFor(Synchronize synchronize, Object followingState) {
		return new NativeFixedStateTransition(synchronize, followingState);
	}

	static StateTransition openAsTimer(long timeout, TimeUnit timeUnit) {
		return openAsTimer(timeout, timeUnit, TransitionThreadPool.getExecutorService());
	}

	static StateTransition openAsTimer(long timeout, TimeUnit timeUnit, ExecutorService executorService) {
		return new SleepingStateTransition(timeout, timeUnit, executorService);
	}

	default Object getFollowState() {return null;}

	void finish();

	void initialize();

	void reset();

	void transit() throws InterruptedException;
}
