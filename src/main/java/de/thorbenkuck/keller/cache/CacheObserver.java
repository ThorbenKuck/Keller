package de.thorbenkuck.keller.cache;

public interface CacheObserver<T> {
	void newEntry(T t, Cache cache);

	void updatedEntry(T t, Cache cache);

	void deletedEntry(Class<T> t, Cache cache);
}
