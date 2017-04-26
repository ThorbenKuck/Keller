package de.thorbenkuck.keller.cache;

import java.util.*;

class CacheImpl extends Observable implements Cache {

	private final Map<Class<?>, Object> internals = new HashMap<>();

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
	public void addCacheObserver(CacheObserver cacheObserver) {
		addObserver(cacheObserver);
	}

	@Override
	public void removeCacheObserver(CacheObserver cacheObserver) {
		deleteObserver(cacheObserver);
	}

	@Override
	public void addGeneralObserver(Observer observer) {
		addObserver(observer);
	}

	@Override
	public void removeGeneralObserver(Observer observer) {
		deleteObserver(observer);
	}

	@Override
	public String toString() {
		return "Cache{" +
				"internals=" + internals +
				'}';
	}

	private void notifyAboutRemovedEntry(Class clazz) {
		sendNotify(new DeletedEntryEvent(clazz));
	}

	private synchronized void sendNotify(Object o) {
		setChanged();
		notifyObservers(o);
		clearChanged();
	}

	private void notifyAboutChangedEntry(Object updatedEntry) {
		sendNotify(new UpdatedEntryEvent(updatedEntry));
	}

	private void notifyAboutNewEntry(Object newEntry) {
		sendNotify(new NewEntryEvent(newEntry));
	}
}
