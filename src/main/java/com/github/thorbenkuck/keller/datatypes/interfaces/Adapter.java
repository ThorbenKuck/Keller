package com.github.thorbenkuck.keller.datatypes.interfaces;

@FunctionalInterface
public interface Adapter<A, R> {
	R work(A a);
}

