package de.thorbenkuck.keller.repository;

public interface ConditionalSupplying<T> extends Supplying<T> {

	RepositoryCondition<T> withRequirement();

}
