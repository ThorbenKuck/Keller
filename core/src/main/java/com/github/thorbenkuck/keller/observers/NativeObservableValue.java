package com.github.thorbenkuck.keller.observers;

import com.github.thorbenkuck.keller.utility.Keller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

final class NativeObservableValue<T> implements ObservableValue<T> {

	private final List<ValueListener<T>> observers = new ArrayList<>();
	private final AtomicBoolean changed = new AtomicBoolean(false);
	private final AtomicReference<T> value = new AtomicReference<>();
	private final AtomicReference<T> temp = new AtomicReference<>();

	NativeObservableValue() {
		this(null);
	}

	NativeObservableValue(final T t) {
		set0(t);
	}

	private ValueListener[] copyObservers() {
		if(!changed.get()) {
			return new ValueListener[0];
		}
		final List<ValueListener<T>> result;
		synchronized (observers) {
			result = new ArrayList<>(observers);
		}

		return result.toArray(new ValueListener[result.size()]);
	}

	private synchronized void notifyObservers() {
		if(!changed.get()) {
			return;
		}
		final ValueListener[] observers = copyObservers();

		T t;
		synchronized (temp) {
			t = temp.get();
		}

		for(final ValueListener listener : observers) {
			((ValueListener<T>)listener).onChange(t, this);
		}
	}

	private void update0() {
		changed.set(true);
		notifyObservers();
	}

	private T get0() {
		T current;
		synchronized (value) {
			current = value.get();
		}

		return current;
	}

	private void set0(final T t) {
		synchronized (value) {
			value.set(t);
		}
	}

	@Override
	public void addObserver(final ValueListener<T> genericObserver) {
		Keller.parameterNotNull(genericObserver);
		synchronized (observers) {
			if(!observers.contains(genericObserver)) {
				observers.add(genericObserver);
			}
		}
	}

	@Override
	public boolean deleteObserver(final ValueListener<T> genericObserver) {
		Keller.parameterNotNull(genericObserver);
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
	public boolean isEmpty() {
		return get0() != null;
	}

	@Override
	public int countObservers() {
		synchronized (observers) {
			return observers.size();
		}
	}

	@Override
	public void set(final T t) {
		Keller.parameterNotNull(t);
		if(get0() == t) {
			return;
		}
		set0(t);

		update0();
	}

	@Override
	public T get() {
		return get0();
	}

	@Override
	public void update() {
		final T current = get0();
		synchronized (temp) {
			temp.set(current);
		}

		update0();
	}

	@Override
	public void clear() {
		set0(null);
	}
}
