package com.github.thorbenkuck.keller.datatypes;

import java.util.*;

public class ConcurrentIterator<T> implements Iterator<T> {

	private final Queue<T> pipe;
	private T current;
	private final Collection<T> root;
	private final boolean removeAllowed;

	public ConcurrentIterator(final Collection<T> collection) {
		this(collection, false);
	}

	public ConcurrentIterator(final Collection<T> collection, boolean removeAllowed) {
		this.pipe = new ArrayDeque<>(collection);
		this.root = collection;
		this.removeAllowed = removeAllowed;
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
		return pipe.peek() != null;
	}

	/**
	 * Returns the next element in the iteration.
	 *
	 * @return the next element in the iteration
	 * @throws NoSuchElementException if the iteration has no more elements
	 */
	@Override
	public T next() {
		select();
		return current;
	}

	private void select() {
		if(!hasNext()) {
			throw new NoSuchElementException();
		}
		synchronized (this) {
			current = pipe.poll();
		}
	}

	/**
	 * Removes from the underlying collection the last element returned
	 * by this iterator (optional operation).  This method can be called
	 * only once per call to {@link #next}.  The behavior of an iterator
	 * is unspecified if the underlying collection is modified while the
	 * iteration is in progress in any way other than by calling this
	 * method.
	 *
	 * @throws UnsupportedOperationException if the {@code remove}
	 *                                       operation is not supported by this iterator
	 * @throws IllegalStateException         if the {@code next} method has not
	 *                                       yet been called, or the {@code remove} method has already
	 *                                       been called after the last call to the {@code next}
	 *                                       method
	 * @implSpec The default implementation throws an instance of
	 * {@link UnsupportedOperationException} and performs no other action.
	 */
	@Override
	public void remove() {
		if(!removeAllowed) {
			throw new UnsupportedOperationException("remove");
		}
		synchronized (this) {
			if (current == null) {
				throw new IllegalStateException("next has never been called");
			}
			root.remove(current);
			current = null;
		}
	}
}
