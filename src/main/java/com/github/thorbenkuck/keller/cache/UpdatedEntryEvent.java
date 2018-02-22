package com.github.thorbenkuck.keller.cache;

public class UpdatedEntryEvent {

	private Object object;

	public UpdatedEntryEvent(Object object) {
		this.object = object;
	}

	public Object getObject() {
		return object;
	}

	@Override
	public String toString() {
		return "UpdatedEntryEvent{" +
				"object=" + object +
				'}';
	}
}
