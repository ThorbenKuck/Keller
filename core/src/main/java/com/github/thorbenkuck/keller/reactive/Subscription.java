package com.github.thorbenkuck.keller.reactive;

import java.util.List;

public interface Subscription {

	boolean isCanceled();

	void cancel();

	List<Throwable> getEncounteredErrors();

	boolean hasEncounteredErrors();

}
