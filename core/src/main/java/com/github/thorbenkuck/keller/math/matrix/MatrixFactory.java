package com.github.thorbenkuck.keller.math.matrix;

import com.github.thorbenkuck.keller.math.Vector;

import java.util.List;

final class MatrixFactory {

	static Matrix fromVectors(List<Vector> vectorList) {
		int rows = getRows(vectorList);
		int columns = vectorList.size();

		Matrix matrix = Matrix.create(rows, columns);

//		for() {
//
//		}

		return matrix;
	}

	private static int getRows(List<Vector> vectors) {
		int rows = 0;
		for(Vector vector : vectors) {
			if(vector.dimensions() > rows) {
				rows = vector.dimensions();
			}
		}

		return rows;
	}
}
