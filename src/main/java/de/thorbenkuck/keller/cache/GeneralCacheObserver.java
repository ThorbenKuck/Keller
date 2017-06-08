package de.thorbenkuck.keller.cache;

public interface GeneralCacheObserver {
	void newEntry(Object t, Cache cache);

	void updatedEntry(Object t, Cache cache);

	void deletedEntry(Class t, Cache cache);
}
