package com.github.thorbenkuck.keller.sync;

import com.github.thorbenkuck.keller.collection.MemoryCacheUnit;

import java.util.*;
import java.util.concurrent.Semaphore;

public class ResourcePool<T> {

	private final Object[] core; 		// Contains only elements of T, even th
	private final boolean[] used;		// Which resources are used
	private final Semaphore semaphore;	// Semaphore for synchronization

	public ResourcePool(Collection<T> resources) {
		core = resources.toArray();
		semaphore = new Semaphore(core.length, true);
		used = new boolean[core.length];
	}

	public ResourcePool(MemoryCacheUnit<T> resources) {
		this(resources.duplicateMemory());
	}

	public ResourcePool(T[]  resources) {
		this(Arrays.asList(resources));
	}

	public T accquireResource() throws InterruptedException {
		semaphore.acquire();
		return getNextFree();
	}

	public void releaseResource(T t) {
		if(markAsUnused(t)) {
			semaphore.release();
		}
	}

	private synchronized T getNextFree() {
		for(int i = 0 ; i < core.length ; i++) {
			if(!used[i]) {
				used[i] = true;
				return (T) core[i];
			}
		}
		return null;
	}

	private synchronized boolean markAsUnused(T item) {
		for(int i = 0 ; i < core.length ; i++) {
			if(core[i] == item) {
				if(used[i]) {
					used[i] = false;
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}
}
