package com.github.thorbenkuck.keller.repository;

import com.github.thorbenkuck.keller.datatypes.interfaces.QueuedAction;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface NotPresentHandler<T> {

	WayPoint<T> throwException(final RuntimeException e);

	WayPoint<T> throwError(final Error error);

	WayPoint<T> getNullObject(final Supplier<T> t);

	WayPoint<T> run(final Runnable runnable);

	WayPoint<T> run(final QueuedAction queuedAction);

	WayPoint<T> handleAllOfSameType(final Consumer<Collection<T>> consumer);

}
