package de.thorbenkuck.keller.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

class ActionStack<T> {

	private final List<Predicate<T>> predicateList = new ArrayList<>();
	private final List<Consumer<T>> consumers = new ArrayList<>();
	private final List<Runnable> ifPresent = new ArrayList<>();
	private final List<Runnable> ifNotPresent = new ArrayList<>();
	private final List<T> matchingObjects = new ArrayList<>();
	private T primaryMatchingElement;
	private T nullObject;
	private Class<T> clazz;

	ActionStack(Class<T> clazz) {
		this.clazz = clazz;
	}

	public List<Predicate<T>> getPredicateList() {
		return predicateList;
	}

	public List<Runnable> getIfPresent() {
		return ifPresent;
	}

	public void addIfPresent(Runnable ifPresent) {
		this.ifPresent.add(ifPresent);
	}

	public List<Runnable> getIfNotPresent() {
		return ifNotPresent;
	}

	public void addIfNotPresent(Runnable ifNotPresent) {
		this.ifNotPresent.add(ifNotPresent);
	}

	public List<T> getMatchingObjects() {
		return matchingObjects;
	}

	public void addMatchingObject(T t) {
		matchingObjects.add(t);
		if(primaryMatchingElement == null) {
			setPrimaryMatchingElement(t);
		}
	}

	public T getPrimaryMatchingElement() {
		return primaryMatchingElement;
	}

	public void setPrimaryMatchingElement(T primaryMatchingElement) {
		this.primaryMatchingElement = primaryMatchingElement;
	}

	public void addPredicate(Predicate<T> predicate) {
		this.predicateList.add(predicate);
	}

	public T getNullObject() {
		return nullObject;
	}

	public void setNullObject(T nullObject) {
		this.nullObject = nullObject;
	}
}
