package com.github.thorbenkuck.keller.math;

import com.github.thorbenkuck.keller.math.d3.ThreeDVector;

import java.util.function.Function;

public interface Vector {

	static ThreeDVector d3(int x, int y, int z) {
		return ThreeDVector.create(x, y, z);
	}

	static ThreeDVector d3(int x, int y) {
		return ThreeDVector.create(x, y);
	}

	static ThreeDVector d3(int x) {
		return ThreeDVector.create(x);
	}

	static ThreeDVector d3(double x, double y, double z) {
		return ThreeDVector.create(x, y, z);
	}

	static ThreeDVector d3(double x, double y) {
		return ThreeDVector.create(x, y);
	}

	static ThreeDVector d3(double x) {
		return ThreeDVector.create(x);
	}

	static ThreeDVector d3() {
		return ThreeDVector.create();
	}

	static ThreeDVector d2(int x, int y) {
		return ThreeDVector.create(x, y);
	}

	static ThreeDVector d2(int x) {
		return ThreeDVector.create(x);
	}

	static ThreeDVector d2(double x, double y) {
		return ThreeDVector.create(x, y);
	}

	static ThreeDVector d2(double x) {
		return ThreeDVector.create(x);
	}

	static ThreeDVector d2() {
		return ThreeDVector.create();
	}

	void negate();

	void toZero();

	void abs();

	void map(Function<Double, Double> consumer);

	void multiplyBy(int scalar);

	void multiplyBy(double scalar);

	void divideBy(int scalar);

	void divideBy(double scalar);

	void subtractBy(int scalar);

	void subtractBy(double scalar);

	void addBy(int scalar);

	void addBy(double scalar);

	int dimensions();

}
