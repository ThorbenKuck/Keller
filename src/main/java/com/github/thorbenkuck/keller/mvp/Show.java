package com.github.thorbenkuck.keller.mvp;

public interface Show {

	/**
	 * This method has to be overridden, to allow the StageController to really "clear" whatever stage you want to clear
	 */
	void close();

	void show();

}
