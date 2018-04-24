package com.github.thorbenkuck.keller.cache;

import com.github.thorbenkuck.keller.annotations.APILevel;
import com.github.thorbenkuck.keller.utility.Keller;

import java.util.*;

@APILevel
final class CacheImpl implements Cache {

	private final Map<Class<?>, Object> internals = new HashMap<>();
	private final Map<Class<?>, List<CacheObserver<?>>> observers = new HashMap<>();
	private final List<GeneralCacheObserver> generalCacheObservers = new ArrayList<>();

	private void set0(Class<?> clazz, Object object) {
		synchronized (internals) {
			internals.put(clazz, object);
		}
	}

	private void remove0(Class<?> clazz) {
		synchronized (internals) {
			internals.remove(clazz);
		}
	}

	private Object get0(Class<?> clazz) {
		Object o;
		synchronized (internals) {
			o = internals.get(clazz);
		}
		return o;
	}

	private <T> void addObserver0(Class<T> clazz, CacheObserver<T> cacheObserver) {
		final List<CacheObserver<?>> setObservers;
		synchronized (observers) {
			setObservers = observers.get(clazz);
		}
		if(setObservers.contains(cacheObserver)) {
			return;
		}
		setObservers.add(cacheObserver);
	}

	private <T> void removeObserver0(Class<T> clazz, CacheObserver<T> cacheObserver) {
		final List<CacheObserver<?>> setObservers;
		synchronized (observers) {
			setObservers = observers.get(clazz);
		}
		setObservers.remove(cacheObserver);
	}

	private void tryAddEmptyObserverList(Class<?> clazz) {
		synchronized (observers) {
			observers.computeIfAbsent(clazz, k -> new ArrayList<>());
		}
	}

	private boolean isSetObservers(Class<?> clazz) {
		final List<CacheObserver<?>> setObservers;
		synchronized (observers) {
			setObservers = observers.get(clazz);
		}

		return setObservers != null;
	}

	private List<CacheObserver<?>> getObserversFor(Class clazz) {
		synchronized (observers) {
			return observers.get(clazz);
		}
	}

	private List<GeneralCacheObserver> getGeneralCacheObservers() {
		synchronized (generalCacheObservers) {
			return generalCacheObservers;
		}
	}

	private void notifyAboutRemovedEntry(Class clazz) {
		for(CacheObserver cacheObserver : getObserversFor(clazz)) {
			cacheObserver.deletedEntry(clazz, this);
		}

		for(GeneralCacheObserver generalCacheObserver : getGeneralCacheObservers()) {
			generalCacheObserver.deletedEntry(clazz, this);
		}
	}

	private void notifyAboutChangedEntry(Object updatedEntry) {
		for(CacheObserver cacheObserver : getObserversFor(updatedEntry.getClass())) {
			cacheObserver.updatedEntry(updatedEntry, this);
		}

		for(GeneralCacheObserver generalCacheObserver : getGeneralCacheObservers()) {
			generalCacheObserver.updatedEntry(updatedEntry, this);
		}
	}

	private void notifyAboutNewEntry(Object newEntry) {
		for(CacheObserver cacheObserver : getObserversFor(newEntry.getClass())) {
			cacheObserver.newEntry(newEntry, this);
		}

		for(GeneralCacheObserver generalCacheObserver : getGeneralCacheObservers()) {
			generalCacheObserver.newEntry(newEntry, this);
		}
	}

	@Override
	public void update(Object object) {
		Keller.parameterNotNull(object);
		Class<?> clazz = object.getClass();
		if (isSet(clazz)) {
			set0(clazz, object);
			notifyAboutChangedEntry(object);
		}
	}

	@Override
	public void addNew(Object object) {
		Keller.parameterNotNull(object);
		Class<?> clazz = object.getClass();
		if (! isSet(clazz)) {
			set0(clazz, object);
			notifyAboutNewEntry(object);
		}
	}

	@Override
	public void addAndOverride(Object object) {
		if (! isSet(object.getClass())) {
			addNew(object);
		} else {
			update(object);
		}
	}

	@Override
	public void remove(Class clazz) {
		if (isSet(clazz)) {
			remove0(clazz);
			notifyAboutRemovedEntry(clazz);
		}
	}

	@Override
	public boolean isSet(Class<?> clazz) {
		return get(clazz).isPresent();
	}

	@Override
	@SuppressWarnings ("unchecked")
	public <T> Optional<T> get(Class<T> clazz) {
		if(clazz == null) {
			return Optional.empty();
		}
		Object retrieved = get0(clazz);
		if (retrieved != null && clazz.equals(retrieved.getClass())) {
			return Optional.of((T) retrieved);
		}
		return Optional.empty();
	}

	@Override
	public <T> void addCacheObserver(Class<T> clazz, CacheObserver<T> cacheObserver) {
		tryAddEmptyObserverList(clazz);
		addObserver0(clazz, cacheObserver);
	}

	@Override
	public <T> void removeCacheObserver(Class<T> clazz, CacheObserver<T> cacheObserver) {
		if(isSetObservers(clazz)) {
			removeObserver0(clazz, cacheObserver);
		}
	}

	@Override
	public void addGeneralCacheObserver(GeneralCacheObserver generalCacheObserver) {
		synchronized (generalCacheObservers) {
			generalCacheObservers.add(generalCacheObserver);
		}
	}

	@Override
	public void removeGeneralCacheObserver(GeneralCacheObserver generalCacheObserver) {
		synchronized (generalCacheObservers) {
			generalCacheObservers.remove(generalCacheObserver);
		}
	}

	@Override
	public String toString() {
		return "Cache{" +
				"internals=" + internals +
				'}';
	}
}
