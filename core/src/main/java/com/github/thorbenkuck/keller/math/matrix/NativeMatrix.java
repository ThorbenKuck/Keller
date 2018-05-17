package com.github.thorbenkuck.keller.math.matrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

class NativeMatrix implements Matrix {

	private double[][] data;
	private int rows;
	private int columns;

	NativeMatrix(int size) {
		this(size, size);
	}

	NativeMatrix(int rows, int columns) {
		data = new double[rows][columns];
		this.rows = rows;
		this.columns = columns;
	}

	NativeMatrix(Matrix matrix) {
		this.data = matrix.toArray();
		this.rows = matrix.getRows();
		this.columns = matrix.getColumns();
	}

	private void requirePointInMatrix(int row, int column) {
		if (! pointInMatrix(row, column)) {
			throw new IllegalArgumentException("Point(" + row + "," + column + ") not in matrix");
		}
	}

	private void requireSameSize(Matrix a, Matrix b) {
		if (! sameSize(a, b)) {
			throw new IllegalArgumentException("Illegal matrix dimensions.");
		}
	}

	@Override
	public int columns() {
		return columns;
	}

	@Override
	public int rows() {
		return rows;
	}

	@Override
	public Matrix randomize(double upper) {
		return randomize(0, upper);
	}

	@Override
	public Matrix randomize(double lower, double upper) {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				data[i][j] = ThreadLocalRandom.current().nextDouble(lower, upper);
			}
		}
		return this;
	}

	@Override
	public Matrix randomize() {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				data[i][j] = ThreadLocalRandom.current().nextInt(10);
			}
		}

		return this;
	}

	@Override
	public void plus(Matrix b) {
		requireSameSize(b, this);
		double[][] baseData = b.toArray();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				data[i][j] += + baseData[i][j];
			}
		}
	}

	@Override
	public void plus(double scalar) {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				data[i][j] += scalar;
			}
		}
	}

	@Override
	public void times(double scalar) {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				data[i][j] *= scalar;
			}
		}
	}

	@Override
	public void times(Matrix matrix) {
		double[][] baseData = matrix.toArray();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				data[i][j] *= baseData[i][j];
			}
		}
	}

	@Override
	public double sum() {
		double sum = 0.0;
		for(double[] array : data) {
			for(double currentData : array) {
				sum += currentData;
			}
		}

		return sum;
	}

	/**
	 * the Sum of all values, treated absolute
	 */
	@Override
	public double absSum() {
		double sum = 0.0;

		for(double[] array : data) {
			for(double currentData : array) {
				sum += Math.abs(currentData);
			}
		}

		return sum;
	}

	@Override
	public void minus(Matrix b) {
		requireSameSize(b, this);
		double[][] baseData = b.toArray();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				data[i][j] -= baseData[i][j];
			}
		}
	}

	@Override
	public void map(MatrixFunction function) {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				double value = data[i][j];
				data[i][j] = function.apply(i, j, value);
			}
		}
	}

	@Override
	public void map(Function<Double, Double> function) {
		map((i, j, d) -> function.apply(d));
	}

	@Override
	public boolean equalTo(Matrix b) {
		if (! sameSize(this, b)) {
			return false;
		}
		Arrays.deepEquals(data, b.toArray());
		return true;
	}

	@Override
	public double getPoint(int row, int column) {
		requirePointInMatrix(row, column);
		return data[row][column];
	}

	@Override
	public void setPoint(int row, int column, double data) {
		requirePointInMatrix(row, column);
		this.data[row][column] = data;
	}

	@Override
	public boolean pointInMatrix(int row, int column) {
		return ! (row < 0 || row >= rows || column < 0 || column >= columns);
	}

	@Override
	public boolean sameSize(Matrix a, Matrix b) {
		return a.getColumns() == b.getColumns() && a.getRows() == b.getRows();
	}

	@Override
	public int getRows() {
		return rows;
	}

	@Override
	public int getColumns() {
		return columns;
	}

	@Override
	public void clear() {
		this.data = null;
		this.rows = 0;
		this.columns = 0;
	}

	@Override
	public double[][] toArray() {
		double[][] toReturn = new double[data.length][];
		for (int i = 0; i < data.length; i++) {
			double[] inner = data[i];
			toReturn[i] = new double[inner.length];
			System.arraycopy(inner, 0, toReturn[i], 0, inner.length);
		}

		return toReturn;
	}

	@Override
	public Double[] to1DArray() {
		List<Double> doubleList = new ArrayList<>();

		for (double[] currentDataRow : data) {
			for (final double currentData : currentDataRow) {
				doubleList.add(currentData);
			}
		}

		return doubleList.toArray(new Double[doubleList.size()]);
	}

	@Override
	public String toString() {
		final StringBuilder stringBuilder = new StringBuilder("Matrix{");
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				stringBuilder.append("{(").append(i).append(",").append(j).append(")").append(data[i][j]).append("}");
			}
		}
		stringBuilder.append("}");

		return stringBuilder.toString();
	}

	@Override
	public String toReadable() {
		final StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				stringBuilder.append(data[i][j]).append(" ");
			}
			stringBuilder.append(System.lineSeparator());
		}

		return stringBuilder.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof NativeMatrix)) return false;

		NativeMatrix matrix = (NativeMatrix) o;
		return equalTo(matrix);
	}

	@Override
	public int hashCode() {
		int result = Arrays.deepHashCode(data);
		result = 31 * result + rows;
		result = 31 * result + columns;
		return result;
	}
}
