package com.github.thorbenkuck.keller.repository;

import java.util.Collection;
public interface Getter<T> {

	T getFirst();

	T getAny();

	Collection<T> getAll();
}
