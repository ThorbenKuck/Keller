package com.github.thorbenkuck.keller.repository;

public interface Supplying<T> extends Getter<T> {

	NotPresentHandler<T> ifNotPresent();

}
