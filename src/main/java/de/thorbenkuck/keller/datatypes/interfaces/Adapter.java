package de.thorbenkuck.keller.datatypes.interfaces;

public interface Adapter<A, R> {
	R work(A a);
}

