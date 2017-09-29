package de.thorbenkuck.keller.repository;

public interface RepositoryConditionConnection<T> {

	RepositoryCondition<T> and();

	Supplying<T> then();

}
