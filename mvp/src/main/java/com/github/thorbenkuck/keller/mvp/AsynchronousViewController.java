package com.github.thorbenkuck.keller.mvp;

import com.github.thorbenkuck.keller.datatypes.interfaces.Factory;
import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.keller.sync.Awaiting;
import com.github.thorbenkuck.keller.sync.Synchronize;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class AsynchronousViewController implements ViewController {

	private final Map<Class<? extends View>, Factory<View>> factoryMap = new HashMap<>();
	private final Value<BiConsumer<Presenter, View>> beforeShowConsumer = Value.of((presenter, view) -> {});
	private final Value<BiConsumer<Presenter, View>> onClose = Value.of(((presenter, view) -> {}));
	private final Value<Consumer<Runnable>> threadSimulator = Value.empty();
	private final Value<View> mainView = Value.empty();
	private final List<View> separateViews = new ArrayList<>();

	public AsynchronousViewController() {
		this(Executors.newCachedThreadPool());
	}

	public AsynchronousViewController(final ExecutorService executorService) {
		this(executorService::execute);
	}

	public AsynchronousViewController(Consumer<Runnable> threadExtractor) {
		this.threadSimulator.set(threadExtractor);
	}

	public void setBeforeShowConsumer(BiConsumer<Presenter, View> beforeShowConsumer) {
		Objects.requireNonNull(beforeShowConsumer);
		this.beforeShowConsumer.set(beforeShowConsumer);
	}

	public BiConsumer<Presenter, View> getBeforeShowConsumer() {
		return beforeShowConsumer.get();
	}

	@Override
	public void setViewFactoryMap(final Map<Class<? extends View>, Factory<View>> factoryMap) {
		Objects.requireNonNull(factoryMap);
		// Todo check all contents for non-null? ... Dunno...
		synchronized (this.factoryMap) {
			this.factoryMap.clear();
			this.factoryMap.putAll(factoryMap);
		}
	}

	@Override
	public void addFactory(final Class<? extends View> clazz, final Factory<View> viewFactory) {
		Objects.requireNonNull(clazz);
		Objects.requireNonNull(viewFactory);
		synchronized (factoryMap) {
			factoryMap.put(clazz, viewFactory);
		}
	}

	@Override
	public Awaiting openMainStage(final Class<? extends View> viewClass) {
		Objects.requireNonNull(viewClass);
		Synchronize synchronize = Synchronize.ofCountDownLatch();

		final Factory<View> viewFactory = safeGetViewFactory(viewClass);

		runOnOtherThread(() -> runSynchronized(() -> {
			closeStageOnCurrentThread(mainView.get());
			View view = viewFactory.produce();
			Presenter presenter = view.getPresenter();
			beforeShowConsumer.get().accept(presenter, view);
			showStage(view);
			mainView.set(view);
			view.notifyOpen();
		}), synchronize);

		return synchronize;
	}

	@Override
	public Awaiting openSeparateStage(final Class<? extends View> viewClass) {
		Objects.requireNonNull(viewClass);
		Synchronize synchronize = Synchronize.ofCountDownLatch();

		final Factory<View> viewFactory = safeGetViewFactory(viewClass);

		runOnOtherThread(() -> {

			Optional<? extends View> oldStageOptional = getActiveSeparateStage(viewClass);
			oldStageOptional.ifPresent(oldStage -> {
				closeStageOnCurrentThread(oldStage);
				separateViews.remove(oldStage);
			});

			View stage = viewFactory.produce();
			separateViews.add(stage);
			showStage(stage);
		}, synchronize);

		return synchronize;
	}

	// This is okay, because we check at runtime... Dam you type erasure!!!!
	@SuppressWarnings ("unchecked")
	@Override
	public <T extends View> Optional<T> getActiveSeparateStage(final Class<T> viewClass) {
		for (View customStage : separateViews) {
			if (customStage.getClass().equals(viewClass)) {
				return Optional.of((T) customStage);
			}
		}
		return Optional.empty();
	}

	@Override
	public Awaiting closeAll() {
		Synchronize synchronize = Synchronize.ofCountDownLatch();
		runOnOtherThread(() -> {
			try {
				closeAllSeparateStages().synchronize();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			closeStageOnCurrentThread(getMainView());
		}, synchronize);
		return synchronize;
	}

	@Override
	public Awaiting closeAllSeparateStages() {
		Synchronize synchronize = Synchronize.ofCountDownLatch();
		runOnOtherThread(() -> {
			for (View stage : separateViews) {
				getActiveSeparateStage(stage.getClass()).ifPresent(this::closeStageSynchronizedOnCurrentThread);
			}
		}, synchronize);
		return synchronize;
	}

	@Override
	public Awaiting closeSeparateActiveStage(final Class<? extends View> viewClass) {
		Synchronize synchronize = Synchronize.ofCountDownLatch();
		runOnOtherThread(() -> getActiveSeparateStage(viewClass).ifPresent(this::closeStageSynchronizedOnCurrentThread), synchronize);
		return synchronize;
	}

	@Override
	public Awaiting closeSeparateActiveStage(final View view) {
		return closeSeparateActiveStage(view.getClass());
	}

	@Override
	public View getMainView() {
		return mainView.get();
	}

	@Override
	public void setThreadExtractor(Consumer<Runnable> extractor) {
		synchronized (threadSimulator) {
			threadSimulator.set(extractor);
		}
	}

	private Factory<View> safeGetViewFactory(final Class<? extends View> clazz) {
		final Factory<View> viewFactory = getFactory(clazz);
		if(viewFactory == null) {
			throw new IllegalStateException("Factory for " + clazz + " is null! This is not acceptable!");
		}
		return viewFactory;
	}

	private void showStage(final View view) {
		view.show();
	}

	private void runSynchronized(Runnable runnable) {
		synchronized (this) {
			runnable.run();
		}
	}

	private void runOnOtherThread(Runnable runnable, Synchronize synchronize) {
		Consumer<Runnable> extractor;
		synchronized (threadSimulator) {
			extractor = threadSimulator.get();
		}
		extractor.accept(() -> {
			runnable.run();
			synchronize.goOn();
		});
	}

	private void closeStageOnCurrentThread(View view) {
		if (view != null) {
			closeStage(view);
		}
	}

	private void closeStage(View view) {
		Presenter presenter = view.getPresenter();
		if (presenter != null) {
			presenter.onClose();
		}
		view.close();
	}

	private void closeStageSynchronizedOnCurrentThread(final View view) {
		runSynchronized(() -> closeStageOnCurrentThread(view));
	}

	private Factory<View> getFactory(Class<? extends View> clazz) {
		Factory<View> result;
		synchronized (factoryMap) {
			result = factoryMap.get(clazz);
		}

		return result;
	}
}
