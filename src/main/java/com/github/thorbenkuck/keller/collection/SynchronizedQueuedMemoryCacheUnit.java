package com.github.thorbenkuck.keller.collection;

import java.io.Serializable;
import java.util.*;

public class SynchronizedQueuedMemoryCacheUnit<T> implements MemoryCacheUnit<T>, Serializable {

	private final Queue<T> cache = new LinkedList<>();
	private final Queue<T> memory = new LinkedList<>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void emptyCache() {
		this.cache.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void emptyMemory() {
		this.memory.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final MemoryCacheUnit resetCache() {
		synchronized (memory) {
			synchronized (cache) {
				cache.clear();
				cache.addAll(memory);
			}
		}
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<T> duplicateMemory() {
		synchronized (memory) {
			return new ArrayList<>(memory);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean containedInCache(T t) {
		synchronized (cache) {
			return cache.contains(t);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean containedInMemory(T t) {
		synchronized (memory) {
			return memory.contains(t);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int cacheSize() {
		synchronized (cache) {
			return cache.size();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {
		synchronized (memory) {
			return memory.size();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void add(T t) {
		synchronized (memory) {
			memory.add(t);
		}
	}

	@Override
	public void addAll(final Collection<T> collection) {
		synchronized (memory) {
			memory.addAll(collection);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<T> iterator() {
		return new SynchronizedInnerIterator(cache);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean hasNext() {
		synchronized (cache) {
			return cache.peek() != null;
		}
	}

	/**
	 * This is the Iterator of this QueuedMemoryCacheUnit.
	 * It takes a Queue, so that it can work on it.
	 */
	protected class SynchronizedInnerIterator implements Iterator<T> {

		private final Queue<T> cache;
		private T current;

		private SynchronizedInnerIterator(final Queue<T> cache) {
			this.cache = cache;
		}

		private void check() {
			if (current == null) {
				throw new NoSuchElementException();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean hasNext() {
			synchronized (cache) {
				return cache.peek() != null;
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public T next() {
			synchronized (cache) {
				current = cache.poll();
			}
			check();
			return current;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final T next() {
		T current;
		synchronized (cache) {
			current = cache.remove();
		}

		if (current == null) {
			throw new NoSuchElementException();
		}
		return current;
	}


}
