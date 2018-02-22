package com.github.thorbenkuck.keller.datatypes.observers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

class GenericObservableValue<T> implements ObservableValue<T> {

	private final List<ValueListener<T>> observers = new ArrayList<>();
	private final AtomicBoolean changed = new AtomicBoolean(false);
	private final AtomicReference<T> value = new AtomicReference<>();
	private final AtomicReference<T> temp = new AtomicReference<>();

	GenericObservableValue(T t) {
		value.set(t);
	}

	private List<ValueListener<T>> copyObservers() {
		List<ValueListener<T>> result;
		if(!changed.get()) {
			return new ArrayList<>();
		}
		synchronized (observers) {
			result = new ArrayList<>(observers);
		}

		return result;
	}

	private synchronized void notifyObservers() {
		List<ValueListener<T>> observers = copyObservers();

		T t;
		synchronized (temp) {
			t = temp.get();
		}

		for(ValueListener<T> copy : observers) {
			copy.onChange(t, this);
		}
	}

	private void _update() {
		changed.set(true);
		notifyObservers();
	}

	@Override
	public void addObserver(final ValueListener<T> genericObserver) {
		synchronized (observers) {
			if(!observers.contains(genericObserver)) {
				observers.add(genericObserver);
			}
		}
	}

	@Override
	public boolean deleteObserver(final ValueListener<T> genericObserver) {
		synchronized (observers) {
			return observers.remove(genericObserver);
		}
	}

	@Override
	public void deleteObservers() {
		synchronized (observers) {
			observers.clear();
		}
	}

	@Override
	public boolean hasChanged() {
		return changed.get();
	}

	@Override
	public int countObservers() {
		synchronized (observers) {
			return observers.size();
		}
	}

	@Override
	public void set(final T t) {
		Objects.requireNonNull(t);
		synchronized (value) {
			value.set(t);
		}

		update();
	}

	@Override
	public T get() {
		synchronized (value) {
			return value.get();
		}
	}

	@Override
	public void update() {
		synchronized (value) {
			synchronized (temp) {
				temp.set(value.get());
			}
		}
		_update();
	}
}
