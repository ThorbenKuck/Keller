package com.github.thorbenkuck.keller.math.d2;

/**
 * This is an immutable point for position in a 2-Dimensional plane
 */
public final class Point2D {

	private final double x;
	private final double y;

	public Point2D(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public final double getX() {
		return x;
	}

	public final double getY() {
		return y;
	}
}
