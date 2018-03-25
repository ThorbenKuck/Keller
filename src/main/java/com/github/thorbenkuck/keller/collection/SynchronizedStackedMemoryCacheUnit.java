package com.github.thorbenkuck.keller.collection;

import java.io.Serializable;
import java.util.*;

public class SynchronizedStackedMemoryCacheUnit<T> implements MemoryCacheUnit<T>, Serializable {

	private final Deque<T> memory = new ArrayDeque<>();
	private final Deque<T> cache = new ArrayDeque<>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void emptyCache() {
		synchronized (cache) {
			cache.clear();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void emptyMemory() {
		synchronized (memory) {
			memory.clear();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MemoryCacheUnit resetCache() {
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
		Collection<T> collection = new ArrayList<>();
		synchronized (memory) {
			collection.addAll(memory);
		}

		return collection;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containedInCache(final T t) {
		synchronized (cache) {
			return cache.contains(t);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containedInMemory(final T t) {
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
	public void add(final T t) {
		synchronized (memory) {
			memory.addFirst(t);
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
	public boolean hasNext() {
		synchronized (cache) {
			return cache.peek() != null;
		}
	}

	private final class SynchronizedInnerIterator implements Iterator<T> {

		private final Deque<T> stack;
		private T current;

		private SynchronizedInnerIterator(final Deque<T> stack) {
			this.stack = stack;
		}

		private void check() {
			if (current == null) {
				throw new NoSuchElementException();
			}
		}

		/**
		 * Returns {@code true} if the iteration has more elements.
		 * (In other words, returns {@code true} if {@link #next} would
		 * return an element rather than throwing an exception.)
		 *
		 * @return {@code true} if the iteration has more elements
		 */
		@Override
		public boolean hasNext() {
			synchronized (stack) {
				return stack.size() != 0;
			}
		}

		/**
		 * Returns the next element in the iteration.
		 *
		 * @return the next element in the iteration
		 * @throws NoSuchElementException if the iteration has no more elements
		 */
		@Override
		public T next() {
			synchronized (stack) {
				current = stack.removeFirst();
			}
			check();
			return current;
		}


	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T next() {
		T next;
		synchronized (cache) {
			next = cache.removeFirst();
		}
		if (next == null) {
			throw new NoSuchElementException();
		}

		return next;
	}


}
