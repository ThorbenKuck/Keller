package com.github.thorbenkuck.keller.collection;

import java.io.Serializable;
import java.util.*;

public class StackedMemoryCacheUnit<T> implements MemoryCacheUnit<T>, Serializable {

	protected final Deque<T> memory = new ArrayDeque<>();
	protected final Deque<T> cache = new ArrayDeque<>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void emptyCache() {
		cache.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void emptyMemory() {
		memory.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MemoryCacheUnit resetCache() {
		cache.clear();
		cache.addAll(memory);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<T> duplicateMemory() {
		return new ArrayList<>(memory);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containedInCache(final T t) {
		return cache.contains(t);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containedInMemory(final T t) {
		return memory.contains(t);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int cacheSize() {
		return cache.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {
		return memory.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void add(final T t) {
		memory.addFirst(t);
	}

	@Override
	public void addAll(final Collection<T> collection) {
		memory.addAll(collection);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<T> iterator() {
		return new InnerIterator(cache);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasNext() {
		return cache.peek() != null;
	}

	private final class InnerIterator implements Iterator<T> {

		private final Deque<T> stack;
		private T current;

		private InnerIterator(final Deque<T> stack) {
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
			return stack.size() != 0;
		}

		/**
		 * Returns the next element in the iteration.
		 *
		 * @return the next element in the iteration
		 * @throws NoSuchElementException if the iteration has no more elements
		 */
		@Override
		public T next() {
			current = stack.removeFirst();
			check();
			return current;
		}


	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T next() {
		T next = cache.removeFirst();
		if (next == null) {
			throw new NoSuchElementException();
		}
		return next;
	}


}
