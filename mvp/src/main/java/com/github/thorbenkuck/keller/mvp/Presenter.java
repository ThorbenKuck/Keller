package com.github.thorbenkuck.keller.mvp;

public interface Presenter<T extends View> {

	/**
	 * May be called in the factory creation
	 */
	default void instantiate(T t) {
		try {
			t.instantiate();
			setView(t);
		} catch (InstantiationException e) {
			errorInViewInstantiation(e);
		}
	}

	default void onClose() {}

	default void errorInViewInstantiation(InstantiationException e) {
		e.printStackTrace();
	}

	void setView(T t);

	T getView();

}
