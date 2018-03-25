package com.github.thorbenkuck.keller.mvp;

public interface View<T extends Presenter> extends Show {

	T getPresenter();

	default void notifyOpen() {}

	// May be overriden.
	// In JavaFX, this is
	// where you would load
	// the JFX file
	default void instantiate() throws InstantiationException {

	}
}
