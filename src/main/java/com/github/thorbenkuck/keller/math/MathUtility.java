package com.github.thorbenkuck.keller.math;

public class MathUtility {

	public static double map(double value, double minimum, double maximum, double newMinimum, double newMaximum) {
		return (value - minimum) * (newMaximum - newMinimum) / (maximum - minimum) + newMinimum;
	}

	public static int map(int value, int minimum, int maximum, int newMinimum, int newMaximum) {
		return (value - minimum) * (newMaximum - newMinimum) / (maximum - minimum) + newMinimum;
	}

	public static double mapToDouble(int value, int minimum, int maximum, int newMinimum, int newMaximum) {
		return (value - minimum) * (newMaximum - newMinimum) / (maximum - minimum) + newMinimum;
	}
}
