package com.github.thorbenkuck.keller.sync;

public class DefaultSemaphoreSynchronize extends AbstractSemaphoreSynchronize {
	@Override
	public void error() {
		System.err.println(this + " encountered an error.");
	}
}
