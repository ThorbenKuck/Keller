package de.thorbenkuck.keller.repository;

public interface WayPoint<T> {

	Finalizer<T> otherwise();

	void now();

	NotPresentHandler<T> andThen();
}
