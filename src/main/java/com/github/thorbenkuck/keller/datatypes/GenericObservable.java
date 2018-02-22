package com.github.thorbenkuck.keller.datatypes;

import com.github.thorbenkuck.keller.datatypes.interfaces.GenericObserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class GenericObservable {

	private boolean changed = false;
	private final Map<Class<?>, List<GenericObserver<?>>> obs = new HashMap<>();

	public <T> void addObserver(Class<T> clazz, GenericObserver<T> genericObserver) {
		if (genericObserver == null) {
			throw new NullPointerException();
		} else {
			this.obs.computeIfAbsent(clazz, k -> new ArrayList<>());

			if (! this.obs.get(clazz).contains(genericObserver)) {
				this.obs.get(clazz).add(genericObserver);
			}
		}
	}

	public <T> boolean deleteObserver(Class<T> clazz, GenericObserver<T> genericObserver) {
		if (obs.get(clazz) != null) {
			return obs.get(clazz).remove(genericObserver);
		} else  {
			return false;
		}
	}

	public void notifyObservers(Object o) {
		Object[] observers = observersToArray(o.getClass());

		for (int i = observers.length - 1; i >= 0; -- i) {
			if (((GenericObserver) observers[i]).accept(o)) {
				((GenericObserver) observers[i]).update(o, this);
			}
		}
	}

	private Object[] observersToArray(Class<?> clazz) {
		Object[] var2;
		synchronized (this) {
			if (! this.changed || this.obs.get(clazz) == null) {
				return new ArrayList().toArray();
			}

			var2 = this.obs.get(clazz).toArray();
			this.clearChanged();
		}
		return var2;
	}

	protected synchronized void clearChanged() {
		this.changed = false;
	}

	protected synchronized void setChanged() {
		this.changed = true;
	}

	public synchronized void deleteObservers() {
		this.obs.clear();
	}

	public synchronized boolean hasChanged() {
		return this.changed;
	}

	public synchronized int countObservers() {
		final int[] toReturn = { 0 };
		this.obs.values().forEach(l -> toReturn[0] += l.size());
		return toReturn[0];
	}

}
