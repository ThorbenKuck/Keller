package de.thorbenkuck.keller.cache;

public class DeletedEntryEvent {

	private Class aClass;

	public DeletedEntryEvent(Class aClass) {
		this.aClass = aClass;
	}

	public Class getCorrespondingClass() {
		return aClass;
	}

	@Override
	public String toString() {
		return "DeletedEntryEvent{" +
				"aClass=" + aClass +
				'}';
	}
}
