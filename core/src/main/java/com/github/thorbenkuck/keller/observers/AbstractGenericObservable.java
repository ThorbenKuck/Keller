package com.github.thorbenkuck.keller.observers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractGenericObservable<T> implements GenericObservable<T> {

	private final AtomicBoolean changed = new AtomicBoolean(false);
	private final List<GenericObserver<T>> obs = new ArrayList<>();

	private List<GenericObserver<T>> threadSafeCopyObservers() {
		final List<GenericObserver<T>> result;
		synchronized (this) {
			if (! hasChanged()) {
				return new ArrayList<>();
			}

			synchronized (obs) {
				result = new ArrayList<>(this.obs);
			}
			this.clearChanged();
		}
		return result;
	}

	@Override
	public void addObserver(final GenericObserver<T> genericObserver) {
		Objects.requireNonNull(genericObserver);
		synchronized (obs) {
			if (! this.obs.contains(genericObserver)) {
				this.obs.add(genericObserver);
			}
		}
	}

	@Override
	public boolean deleteObserver(final GenericObserver<T> genericObserver) {
		Objects.requireNonNull(genericObserver);
		synchronized (obs) {
			return obs.remove(genericObserver);
		}
	}

	@Override
	public synchronized void deleteObservers() {
		synchronized (obs) {
			this.obs.clear();
		}
	}

	@Override
	public boolean hasChanged() {
		return this.changed.get();
	}

	@Override
	public int countObservers() {
		synchronized (obs) {
			return obs.size();
		}
	}

	protected void notifyObservers(final T o) {
		final List<GenericObserver<T>> observers = threadSafeCopyObservers();

		for (final GenericObserver<T> observer : observers) {
			if (observer.accepts(o)) {
				observer.update(o, this);
			}
		}
	}

	protected void notifyObserver() {
		notifyObservers(null);
	}

	protected void clearChanged() {
		this.changed.set(false);
	}

	protected void setChanged() {
		this.changed.set(true);
	}

}
