package de.thorbenkuck.keller.cache;

import java.util.*;

class CacheImpl implements Cache {

	private final Map<Class<?>, Object> internals = new HashMap<>();
	private final Map<Class<?>, List<CacheObserver<?>>> observers = new HashMap<>();
	private final List<GeneralCacheObserver> generalCacheObservers = new ArrayList<>();

	@Override
	public void update(Object object) {
		if (isSet(object.getClass())) {
			synchronized (internals) {
				internals.put(object.getClass(), object);
			}
			notifyAboutChangedEntry(object);
		}
	}

	@Override
	public void addNew(Object object) {
		if (! isSet(object.getClass())) {
			synchronized (internals) {
				internals.put(object.getClass(), object);
			}
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
			synchronized (internals) {
				internals.remove(clazz);
			}
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
		Object retrieved;
		synchronized (internals) {
			retrieved = internals.get(clazz);
		}
		if (retrieved != null && retrieved.getClass().equals(clazz)) {
			return Optional.of((T) retrieved);
		}
		return Optional.empty();
	}

	@Override
	public <T> void addCacheObserver(Class<T> clazz, CacheObserver<T> cacheObserver) {
		observers.computeIfAbsent(clazz, k -> new ArrayList<>());
		observers.get(clazz).add(cacheObserver);
	}

	@Override
	public <T> void removeCacheObserver(Class<T> clazz, CacheObserver<T> cacheObserver) {
		if(observers.get(clazz) != null) {
			observers.get(clazz).remove(cacheObserver);
		}
	}

	@Override
	public void addGeneralCacheObserver(GeneralCacheObserver generalCacheObserver) {
		generalCacheObservers.add(generalCacheObserver);
	}

	@Override
	public void removeGeneralCacheObserver(GeneralCacheObserver generalCacheObserver) {
		generalCacheObservers.remove(generalCacheObserver);
	}

	@Override
	public String toString() {
		return "Cache{" +
				"internals=" + internals +
				'}';
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

	private List<CacheObserver<?>> getObserversFor(Class clazz) {
		return observers.get(clazz);
	}

	private List<GeneralCacheObserver> getGeneralCacheObservers() {
		return generalCacheObservers;
	}
}
