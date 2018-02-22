package com.github.thorbenkuck.keller.repository;

import java.util.function.Predicate;

public interface RepositoryCondition<T> {

	RepositoryConditionConnection<T> objectFulfills(Predicate<T> predicate);

	RepositoryConditionConnection<T> objectDoesNotMeet(Predicate<T> predicate);

}
