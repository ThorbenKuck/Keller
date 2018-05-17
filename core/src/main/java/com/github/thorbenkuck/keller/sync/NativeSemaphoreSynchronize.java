package com.github.thorbenkuck.keller.sync;

import com.github.thorbenkuck.keller.datatypes.interfaces.QueuedAction;
import com.github.thorbenkuck.keller.datatypes.interfaces.Value;

final class NativeSemaphoreSynchronize extends AbstractSemaphoreSynchronize {

	private Value<QueuedAction> onError = Value.synchronize(() -> System.out.println(this + " encountered an error!"));

	@Override
	public final void error() {
		QueuedAction.call(onError);
	}

	@Override
	public final void setOnError(QueuedAction queuedAction) {
		onError.set(queuedAction);
	}
}
