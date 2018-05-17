package com.github.thorbenkuck.keller.repository;

import java.util.function.Predicate;

public interface RepositoryCondition<T> {

	RepositoryConditionConnection<T> objectFulfills(final Predicate<T> predicate);

	RepositoryConditionConnection<T> objectDoesNotMeet(final Predicate<T> predicate);

}
