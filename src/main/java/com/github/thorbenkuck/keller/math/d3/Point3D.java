package com.github.thorbenkuck.keller.math.d3;

public final class Point3D {

	private final double x;
	private final double y;
	private final double z;

	public Point3D(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public final double getX() {
		return x;
	}

	public final double getY() {
		return y;
	}

	public final double getZ() {
		return z;
	}
}
