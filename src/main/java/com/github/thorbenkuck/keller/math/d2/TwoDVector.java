package com.github.thorbenkuck.keller.math.d2;

import com.github.thorbenkuck.keller.datatypes.interfaces.PrettyPrint;
import com.github.thorbenkuck.keller.math.GenericVector;
import com.github.thorbenkuck.keller.math.Vector;

public interface TwoDVector extends Vector, GenericVector<TwoDVector, TwoDVectorFunction>, PrettyPrint {

	static TwoDVector add(TwoDVector a, TwoDVector b) {
		final TwoDVector base = new NativeTwoDVector(a);
		base.addBy(b);
		return base;
	}

	static TwoDVector subtract(TwoDVector a, TwoDVector b) {
		final TwoDVector base = new NativeTwoDVector(a);
		base.subtractBy(b);
		return base;
	}

	static TwoDVector diff(TwoDVector a, TwoDVector b) {
		final TwoDVector temp = subtract(a, b);
		temp.abs();
		return temp;
	}

	static TwoDVector convert(Point2D point2D) {
		return new NativeTwoDVector(point2D);
	}

	static TwoDVector create(int x, int y) {
		return new NativeTwoDVector(x, y);
	}

	static TwoDVector create(int x) {
		return create(x, 0);
	}

	static TwoDVector create(double x, double y) {
		return new NativeTwoDVector(x, y);
	}

	static TwoDVector create(double x) {
		return new NativeTwoDVector(x, 0);
	}

	static TwoDVector create() {
		return new NativeTwoDVector();
	}

	static Point2D shift(Point2D point2D, TwoDVector vector) {
		return add(new NativeTwoDVector(point2D), vector).toPoint();
	}

	static TwoDVector fromPoints(Point2D a, Point2D b) {
		return add(new NativeTwoDVector(a), new NativeTwoDVector(b));
	}

	Point2D toPoint();

	double getX();

	double getY();

	default int dimensions() {
		return 2;
	}
}
