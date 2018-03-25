package com.github.thorbenkuck.keller.math;

/**
 * This class should be used, for whatever dimension you want to create a Vector
 *
 * @param <T> The Type of the Vector you are creating
 * @param <F> The Function, that handles your Vector-type
 */
public interface GenericVector<T, F> {

	void addBy(T vector);

	void subtractBy(T scalar);

	void multiplyBy(T vector);

	void divideBy(T scalar);

	void map(F function);

	T copy();

}
