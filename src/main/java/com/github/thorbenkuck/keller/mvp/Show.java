package com.github.thorbenkuck.keller.mvp;

public interface Show {

	/**
	 * This method has to be overridden, to allow the StageController to really "close" whatever stage you want to close
	 */
	void close();

	void show();

}
