package com.github.thorbenkuck.keller.math.d3;

import java.util.function.Function;

class ThreeDVectorImpl implements ThreeDVector {

	private double x;
	private double y;
	private double z;

	ThreeDVectorImpl(Point3D point3D) {
		this.x = point3D.getX();
		this.y = point3D.getY();
		this.z = point3D.getZ();
	}

	ThreeDVectorImpl(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	ThreeDVectorImpl(double x, double y) {
		this(x, y, 0);
	}

	ThreeDVectorImpl() {
		this(0, 0);
	}

	ThreeDVectorImpl(ThreeDVectorImpl vector) {
		this(vector.x, vector.y, vector.z);
	}

	ThreeDVectorImpl(ThreeDVector vector) {
		this(vector.getX(), vector.getY(), vector.getZ());
	}

	@Override
	public void addBy(ThreeDVector vector) {
		this.x += vector.getX();
		this.y += vector.getY();
		this.z += vector.getZ();
	}

	@Override
	public void addBy(int scalar) {
		addBy(new ThreeDVectorImpl(scalar, scalar, scalar));
	}

	@Override
	public void addBy(double scalar) {
		addBy(new ThreeDVectorImpl(scalar, scalar, scalar));
	}

	@Override
	public void subtractBy(ThreeDVector scalar) {
		final ThreeDVectorImpl temp = new ThreeDVectorImpl(scalar);
		temp.negate();
		addBy(temp);
	}

	@Override
	public void subtractBy(int scalar) {
		subtractBy(new ThreeDVectorImpl(scalar, scalar, scalar));
	}

	@Override
	public void subtractBy(double scalar) {
		subtractBy(new ThreeDVectorImpl(scalar, scalar, scalar));
	}

	@Override
	public void multiplyBy(ThreeDVector vector) {
		this.x *= vector.getX();
		this.y *= vector.getY();
		this.z *= vector.getZ();
	}

	@Override
	public void multiplyBy(int scalar) {
		multiplyBy(new ThreeDVectorImpl(scalar, scalar, scalar));
	}

	@Override
	public void multiplyBy(double scalar) {
		multiplyBy(new ThreeDVectorImpl(scalar, scalar, scalar));
	}

	@Override
	public void divideBy(int scalar) {
		divideBy(new ThreeDVectorImpl(scalar, scalar, scalar));
	}

	@Override
	public void divideBy(double scalar) {
		divideBy(new ThreeDVectorImpl(scalar, scalar, scalar));
	}

	@Override
	public void divideBy(ThreeDVector scalar) {
		this.x /= scalar.getX();
		this.y /= scalar.getY();
		this.z /= scalar.getZ();
	}

	@Override
	public void negate() {
		this.x = -x;
		this.y = -y;
		this.z = -z;
	}

	@Override
	public void toZero() {
		map(entry -> 0.0);
	}

	@Override
	public void abs() {
		map(Math::abs);
	}

	@Override
	public void map(Function<Double, Double> consumer) {
		x = consumer.apply(x);
		y = consumer.apply(y);
		z = consumer.apply(z);
	}

	@Override
	public void map(ThreeDVectorFunction function) {
		x = function.mapX(x);
		y = function.mapY(y);
		z = function.mapZ(z);
	}

	@Override
	public Point3D toPoint() {
		return new Point3D(this.x, this.y, this.z);
	}

	@Override
	public String toString() {
		return "Vector{x=" + this.x + ", y=" + this.y + ", z=" + this.z + "}";
	}

	@Override
	public String prettyPrint() {
		final StringBuilder stringBuilder = new StringBuilder();
		final String ls = System.lineSeparator();
		stringBuilder.append(x).append(ls).append(y).append(ls).append(z).append(ls);
		return stringBuilder.toString();
	}

	@Override
	public ThreeDVector copy() {
		return new ThreeDVectorImpl(this);
	}

	@Override
	public double getX() {
		return x;
	}

	@Override
	public double getY() {
		return y;
	}

	@Override
	public double getZ() {
		return z;
	}
}
