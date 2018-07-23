package com.github.thorbenkuck.keller.event.reactor;

public interface Collect<T extends Event> {

	T collect();

}
