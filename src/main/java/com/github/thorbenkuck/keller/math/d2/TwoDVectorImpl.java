package com.github.thorbenkuck.keller.math.d2;

import java.util.function.Function;

class TwoDVectorImpl implements TwoDVector {

	private double x;
	private double y;

	TwoDVectorImpl(Point2D point2D) {
		this.x = point2D.getX();
		this.y = point2D.getY();
	}

	TwoDVectorImpl(double x, double y) {
		this(new Point2D(x, y));
	}

	TwoDVectorImpl() {
		this(0, 0);
	}

	TwoDVectorImpl(TwoDVectorImpl vector) {
		this(vector.x, vector.y);
	}

	TwoDVectorImpl(TwoDVector vector) {
		this(vector.getX(), vector.getY());
	}

	@Override
	public void addBy(TwoDVector vector) {
		this.x += vector.getX();
		this.y += vector.getY();
	}

	@Override
	public void addBy(int scalar) {
		addBy(new TwoDVectorImpl(scalar, scalar));
	}

	@Override
	public void addBy(double scalar) {
		addBy(new TwoDVectorImpl(scalar, scalar));
	}

	@Override
	public void subtractBy(TwoDVector scalar) {
		final TwoDVectorImpl temp = new TwoDVectorImpl(scalar);
		temp.negate();
		addBy(temp);
	}

	@Override
	public void subtractBy(int scalar) {
		subtractBy(new TwoDVectorImpl(scalar, scalar));
	}

	@Override
	public void subtractBy(double scalar) {
		subtractBy(new TwoDVectorImpl(scalar, scalar));
	}

	@Override
	public void multiplyBy(TwoDVector vector) {
		this.x *= vector.getX();
		this.y *= vector.getY();
	}

	@Override
	public void multiplyBy(int scalar) {
		multiplyBy(new TwoDVectorImpl(scalar, scalar));
	}

	@Override
	public void multiplyBy(double scalar) {
		multiplyBy(new TwoDVectorImpl(scalar, scalar));
	}

	@Override
	public void divideBy(int scalar) {
		divideBy(new TwoDVectorImpl(scalar, scalar));
	}

	@Override
	public void divideBy(double scalar) {
		divideBy(new TwoDVectorImpl(scalar, scalar));
	}

	@Override
	public void divideBy(TwoDVector scalar) {
		this.x /= scalar.getX();
		this.y /= scalar.getY();
	}

	@Override
	public void negate() {
		this.x = -x;
		this.y = -y;
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
	}

	@Override
	public void map(TwoDVectorFunction function) {
		x = function.mapX(x);
		y = function.mapY(y);
	}

	@Override
	public Point2D toPoint() {
		return new Point2D(this.x, this.y);
	}

	@Override
	public String toString() {
		return "Vector{x=" + this.x + ", y=" + this.y + "}";
	}

	@Override
	public String prettyPrint() {
		final StringBuilder stringBuilder = new StringBuilder();
		final String ls = System.lineSeparator();
		stringBuilder.append(x).append(ls).append(y).append(ls);
		return stringBuilder.toString();
	}

	@Override
	public TwoDVector copy() {
		return new TwoDVectorImpl(this);
	}

	@Override
	public double getX() {
		return x;
	}

	@Override
	public double getY() {
		return y;
	}
}
