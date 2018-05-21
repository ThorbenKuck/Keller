package com.github.thorbenkuck.keller.state;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.keller.di.DependencyManager;
import com.github.thorbenkuck.keller.di.annotations.Bind;
import com.github.thorbenkuck.keller.di.annotations.Cache;
import com.github.thorbenkuck.keller.pipe.Pipeline;
import com.github.thorbenkuck.keller.sync.Synchronize;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Cache
final class NativeStateMachine implements @Bind StateMachine {

	private final Value<Boolean> running = Value.synchronize(false);
	private final Pipeline<StateMachine> finishedPipeline = Pipeline.unifiedCreation();
	private final StateStorage stateStorage = new StateStorage();

	@Override
	public boolean isRunning() {
		return running.get();
	}

	@Override
	public void setStateContext(Object object) {
		if (running.get()) {
			throw new IllegalStateException("Cannot change the StateContext while StateMachine is running!");
		}
		stateStorage.setStateContext(object);
	}

	@Override
	public void stop() {
		running.set(false);
		stateStorage.getCurrentStateTransition().finish();
	}

	private void dispatchStart(Object object, Synchronize synchronize) {
		if(isRunning()) {
			return;
		}
		addStateDependency(this);
		stateStorage.setCurrentState(object);
		running.set(true);
		stateStorage.injectStateIntoStateContext();
		synchronize.goOn();
		try {
			while (!stateStorage.hasNext() && running.get()) {
				stateStorage.handleCurrentState();
			}
		} catch (final Throwable throwable) {
			throw new IllegalStateException("Unexpected Throwable in StateMachine", throwable);
		} finally {
			running.set(false);
		}
		finishedPipeline.apply(this);
	}

	@Override
	public void start(final Object object) {
		dispatchStart(object, Synchronize.empty());
	}

	@Override
	public void parallel(final Object object) {
		final Synchronize synchronize = Synchronize.createDefault();
		final Thread thread = new Thread(() -> dispatchStart(object, synchronize));
		thread.setName("keller-state-runner");
		try {
			thread.start();
			synchronize.synchronize();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addFinishedCallback(Consumer<StateMachine> callback) {
		finishedPipeline.addLast(callback);
	}

	@Override
	public void addStateDependency(final Object object) {
		stateStorage.addDependency(object);
	}

	@Override
	public void setDependencyManager(final DependencyManager dependencyManager) {
		stateStorage.setDependencyManager(dependencyManager);
	}

	@Override
	public void step() {
		stateStorage.getCurrentStateTransition().finish();
	}
}
