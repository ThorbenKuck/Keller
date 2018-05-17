package com.github.thorbenkuck.keller.mvp;

import com.github.thorbenkuck.keller.datatypes.interfaces.Factory;
import com.github.thorbenkuck.keller.sync.Awaiting;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public interface ViewController {

	static ViewController createViewController() {
		return new AsynchronousViewController(ThreadPoolCache.getExecutorService());
	}

	void setViewFactoryMap(final Map<Class<? extends View>, Factory<View>> factoryMap);

	void addFactory(final Class<? extends View> clazz, final Factory<View> viewFactory);

	Awaiting openMainStage(final Class<? extends View> stageClass);

	Awaiting openSeparateStage(final Class<? extends View> stageClazz);

	<T extends View> Optional<T> getActiveSeparateStage(final Class<T> stageClazz);

	Awaiting closeAll();

	Awaiting closeAllSeparateStages();

	Awaiting closeSeparateActiveStage(final Class<? extends View> stage);

	Awaiting closeSeparateActiveStage(final View stage);

	View getMainView();

	void setThreadExtractor(Consumer<Runnable> extractor);
}
