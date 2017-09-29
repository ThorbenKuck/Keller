package de.thorbenkuck.keller.repository;

import de.thorbenkuck.keller.datatypes.interfaces.QueuedAction;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface NotPresentHandler<T> {

	WayPoint<T> throwException(RuntimeException e);

	WayPoint<T> throwError(Error error);

	WayPoint<T> getNullObject(Supplier<T> t);

	WayPoint<T> run(Runnable runnable);

	WayPoint<T> run(QueuedAction queuedAction);

	WayPoint<T> handleAllOfSameType(Consumer<Collection<T>> consumer);

}
