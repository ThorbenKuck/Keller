package com.github.thorbenkuck.keller.cache;

import java.util.Optional;

public interface Cache {

	static Cache create() {
		return new CacheImpl();
	}

	void update(Object object);

	void addNew(Object object);

	void addAndOverride(Object object);

	void remove(Class clazz);

	boolean isSet(Class<?> clazz);

	<T> Optional<T> get(Class<T> clazz);

	<T> void addCacheObserver(Class<T> clazz, CacheObserver<T> cacheObserver);

	<T> void removeCacheObserver(Class<T> clazz, CacheObserver<T> cacheObserver);

	void addGeneralCacheObserver(GeneralCacheObserver generalCacheObserver);

	void removeGeneralCacheObserver(GeneralCacheObserver generalCacheObserver);
}
