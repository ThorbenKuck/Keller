package com.github.thorbenkuck.keller.collection;

import java.io.Serializable;
import java.util.*;

/**
 * Die QueuedMemoryCacheUnit ist etwas wie ein resettable Stack / Queue. Eine interne Repräsentation der Elemente passiert auf 2 ebenen.
 * So kann über die Methode resetCache die QueuedMemoryCacheUnit "cloned" werden, ohne dass dies tatsächlich cloned werden muss.
 * <p>
 * Jedoch ist diese Klasse nicht 100%ig Thread-Safe. Zwar sind die Atribute volatile, aber die Methoden nicht synchronized.
 * Das hat den ganz einfachen Grund, dass die Performanz nicht leiden soll.
 * <p>
 * Man kann jedoch in der erbenden Klasse alle Methoden synchronized zu machen.
 *
 * @param <T> Der Objekt-Typ, welcher intern gespeichert werden soll.
 */
public class QueuedMemoryCacheUnit<T> implements MemoryCacheUnit<T>, Serializable {

	protected Queue<T> cache;
	protected Queue<T> memory;

	public QueuedMemoryCacheUnit() {
		cache = new LinkedList<>();
		memory = new LinkedList<>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void emptyCache() {
		this.cache = new LinkedList<>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void emptyMemory() {
		this.memory = new LinkedList<>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final MemoryCacheUnit resetCache() {
		this.cache = new LinkedList<>(memory);
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
	public final boolean containedInCache(T t) {
		return cache.contains(t);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean containedInMemory(T t) {
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
	public final void add(T t) {
		memory.add(t);
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
	public final boolean hasNext() {
		return cache.peek() != null;
	}

	/**
	 * This is the Iterator of this QueuedMemoryCacheUnit.
	 * It takes a Queue, so that it can work on it.
	 */
	protected class InnerIterator implements Iterator<T> {

		private final Queue<T> cache;
		private T current;

		private InnerIterator(final Queue<T> cache) {
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
			return cache.peek() != null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public T next() {
			current = cache.poll();
			check();
			return current;
		}


	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final T next() {
		return cache.remove();
	}


}
