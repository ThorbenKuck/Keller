package com.github.thorbenkuck.keller.cache;

public class NewEntryEvent {

	private Object object;

	public NewEntryEvent(Object object) {
		this.object = object;
	}

	public Object getObject() {
		return object;
	}

	@Override
	public String toString() {
		return "NewEntryEvent{" +
				"object=" + object +
				'}';
	}
}
