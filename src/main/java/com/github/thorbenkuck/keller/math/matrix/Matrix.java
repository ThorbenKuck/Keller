package com.github.thorbenkuck.keller.math.matrix;

import com.github.thorbenkuck.keller.datatypes.interfaces.PrettyPrint;

import java.util.function.Function;

public interface Matrix extends PrettyPrint {

	static Matrix create(int rows, int columns) {
		return new MatrixImpl(rows, columns);
	}

	static Matrix createCubic(int size) {
		return new MatrixImpl(size);
	}

	static Matrix copy(Matrix matrix) {
		return new MatrixImpl(matrix);
	}

	static Matrix multiply(Matrix a, Matrix b) {
		if (a.getColumns() != b.getRows()) {
			throw new RuntimeException("Illegal matrix dimensions.");
		}
		Matrix c = new MatrixImpl(a.getRows(), b.getColumns());
		for (int i = 0; i < c.getRows(); i++) {
			for (int j = 0; j < c.getColumns(); j++) {
				for (int k = 0; k < a.getColumns(); k++) {
					double cPoint = c.getPoint(i, j);
					c.setPoint(i, j, ((a.getPoint(i,k) * b.getPoint(k,j)) + cPoint));
				}
			}
		}
		return c;
	}

	static Matrix transpose(Matrix base) {
		int rows = base.getRows();
		int columns = base.getColumns();
		Matrix a = new MatrixImpl(columns, rows);
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				a.setPoint(j, i, base.getPoint(i, j));
			}
		}
		return a;
	}

	static Matrix fromArray(Double[] array) {
		Matrix matrix = new MatrixImpl(array.length, 1);
		for (int i = 0; i < array.length; i++) {
			matrix.setPoint(i, 0, array[i]);
		}
		return matrix;
	}

	static Matrix subtract(Matrix a, Matrix b) {
		Matrix c = new MatrixImpl(a);
		c.minus(b);

		return c;
	}

	static Matrix map(Matrix a, Function<Double, Double> function) {
		Matrix result = new MatrixImpl(a);
		result.map(function);

		return result;
	}

	static Matrix map(Matrix a, MatrixFunction function) {
		Matrix result = new MatrixImpl(a);
		result.map(function);

		return result;
	}

	int columns();

	int rows();

	Matrix randomize(double upper);

	Matrix randomize(double lower, double upper);

	Matrix randomize();

	void plus(Matrix b);

	void plus(double scalar);

	void times(double scalar);

	void times(Matrix matrix);

	double sum();

	/**
	 * the Sum of all values, treated absolute
	 */
	double absSum();

	void minus(Matrix b);

	void map(MatrixFunction function);

	void map(Function<Double, Double> function);

	boolean equalTo(Matrix b);

	double getPoint(int row, int column);

	void setPoint(int row, int column, double data);

	boolean pointInMatrix(int row, int column);

	boolean sameSize(Matrix a, Matrix b);

	int getRows();

	int getColumns();

	void clear();

	double[][] toArray();

	Double[] to1DArray();
}
