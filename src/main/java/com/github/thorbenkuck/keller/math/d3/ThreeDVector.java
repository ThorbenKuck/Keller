package com.github.thorbenkuck.keller.math.d3;

import com.github.thorbenkuck.keller.datatypes.interfaces.PrettyPrint;
import com.github.thorbenkuck.keller.math.GenericVector;
import com.github.thorbenkuck.keller.math.Vector;

public interface ThreeDVector extends Vector, GenericVector<ThreeDVector, ThreeDVectorFunction>, PrettyPrint {

	static ThreeDVector add(ThreeDVector a, ThreeDVector b) {
		final ThreeDVector base = new NativeThreeDVector(a);
		base.addBy(b);
		return base;
	}

	static ThreeDVector subtract(ThreeDVector a, ThreeDVector b) {
		final ThreeDVector base = new NativeThreeDVector(a);
		base.subtractBy(b);
		return base;
	}

	static ThreeDVector diff(ThreeDVector a, ThreeDVector b) {
		final ThreeDVector temp = subtract(a, b);
		temp.abs();
		return temp;
	}

	static ThreeDVector convert(Point3D point3D) {
		return new NativeThreeDVector(point3D);
	}

	static ThreeDVector create(int x, int y, int z) {
		return new NativeThreeDVector(x, y, z);
	}

	static ThreeDVector create(int x, int y) {
		return new NativeThreeDVector(x, y);
	}

	static ThreeDVector create(int x) {
		return create(x, 0);
	}

	static ThreeDVector create(double x, double y, double z) {
		return new NativeThreeDVector(x, y, z);
	}

	static ThreeDVector create(double x, double y) {
		return new NativeThreeDVector(x, y);
	}

	static ThreeDVector create(double x) {
		return new NativeThreeDVector(x, 0);
	}

	static ThreeDVector create() {
		return new NativeThreeDVector();
	}

	static Point3D shift(Point3D point3D, ThreeDVector vector) {
		return add(new NativeThreeDVector(point3D), vector).toPoint();
	}

	static ThreeDVector fromPoints(Point3D a, Point3D b) {
		return add(new NativeThreeDVector(a), new NativeThreeDVector(b));
	}

	Point3D toPoint();

	double getX();

	double getY();

	double getZ();

	default int dimensions() {
		return 3;
	}
}
