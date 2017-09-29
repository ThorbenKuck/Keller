package de.thorbenkuck.keller.implementation.collection;

import java.io.Serializable;
import java.util.*;

/**
 * Die QueuedMemoryCacheUnit ist etwas wie ein resettable Stack / Queue. Eine interne Repräsentation der Elemente passiert auf 2 ebenen.
 * So kann über die Methode resetCache die QueuedMemoryCacheUnit "cloned" werden, ohne dass dies tatsächlich cloned werden muss.
 *
 * Jedoch ist diese Klasse nicht 100%ig Thread-Safe. Zwar sind die Atribute volatile, aber die Methoden nicht synchronized.
 * Das hat den ganz einfachen Grund, dass die Performanz nicht leiden soll.
 *
 * Man kann jedoch in der erbenden Klasse alle Methoden synchronized zu machen.
 *
 * @param <T> Der Objekt-Typ, welcher intern gespeichert werden soll.
 */
public class QueuedMemoryCacheUnit<T> implements MemoryCacheUnit<T>, Serializable {

	protected volatile Queue<T> cache;
	protected volatile Queue<T> memory;

	public QueuedMemoryCacheUnit() {
		cache = new LinkedList<>();
		memory = new LinkedList<>();
	}

	/**
	 * <p>
	 * Diese Methode soll nicht in einer Prozedur verwendet werden, sondern lediglich von erbenden Klassen.
	 * Deswegen ist diese Methode protected
	 * </p>
	 * <p>Der cache ist der Speicher, auf welchen von außen zugegriffen wird.</p>
	 * <p>
	 * der Aufruf dieser Methode, löscht den Cache
	 * </p>
	 */
	protected final void emptyCache() {
		this.cache = new LinkedList<>();
	}

	/**
	 * <p>
	 * Diese Methode soll nicht in einer Prozedur verwendet werden, sondern lediglich von erbenden Klassen.
	 * Deswegen ist diese Methode protected
	 * </p>
	 * <p>
	 * Der memory ist der Speicher, auf welchen von außen nicht direkt zugegriffen werden soll.
	 * Er speichert Informationen, für den Fall, dass der Cache zurück gesetzt wird
	 * </p>
	 * <p>
	 * der Aufruf dieser Methode, löscht den internen Speicher.
	 * </p>
	 */
	protected final void emptyMemory() {
		this.memory = new LinkedList<>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final QueuedMemoryCacheUnit resetCache() {
		this.cache = new LinkedList<>(memory);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void add(T t) {
		memory.add(t);
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
	public final T next() {
		return cache.remove();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<T> iterator() {
		return new InnerIterator<>(cache);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean hasNext() {
		return cache.peek() != null;
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
	public int size() {
		return memory.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int cacheSize() {
		return cache.size();
	}

	/**
	 * This is the Iterator of this QueuedMemoryCacheUnit.
	 * It takes a Queue, so that it can work on it.
	 *
	 * @param <I>
	 */
	protected class InnerIterator<I> implements Iterator<I> {

		private final Queue<I> cache;

		private InnerIterator(final Queue<I> cache) {
			this.cache = cache;
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
		public I next() {
			return cache.poll();
		}
	}
}
