package com.github.thorbenkuck.keller.mvp;

public interface View extends Show {

	Presenter getPresenter();

	default void notifyOpen() {}

	// May be overriden.
	// In JavaFX, this is
	// where you would load
	// the JFX file
	default void instantiate() throws InstantiationException {

	}
}
