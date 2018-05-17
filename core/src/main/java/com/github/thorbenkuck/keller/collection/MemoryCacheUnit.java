package com.github.thorbenkuck.keller.collection;

import java.util.Collection;
import java.util.Iterator;

public interface MemoryCacheUnit<T> extends Iterable<T>, Iterator<T> {

	static <T> MemoryCacheUnit<T> queue() {
		return new QueuedMemoryCacheUnit<>();
	}

	static <T> MemoryCacheUnit<T> synchronizedQueue() {
		return new SynchronizedQueuedMemoryCacheUnit<>();
	}

	static <T> MemoryCacheUnit<T> queue(Collection<T> collection) {
		MemoryCacheUnit<T> memoryCacheUnit = queue();
		memoryCacheUnit.addAll(collection);
		memoryCacheUnit.resetCache();

		return memoryCacheUnit;
	}

	static <T> MemoryCacheUnit<T> stack() {
		return new StackedMemoryCacheUnit<>();
	}

	static <T> MemoryCacheUnit<T> synchronizedStack() {
		return new SynchronizedStackedMemoryCacheUnit<>();
	}

	static <T> MemoryCacheUnit<T> stack(Collection<T> collection) {
		MemoryCacheUnit<T> memoryCacheUnit = stack();
		memoryCacheUnit.addAll(collection);
		memoryCacheUnit.resetCache();

		return memoryCacheUnit;
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
	void emptyCache();

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
	void emptyMemory();

	/**
	 * Diese Methode setzt den Cache zurück. Alle jemals gespeicherten und nicht wieder entfernten Elemente werden damit in den Cache gesetzt
	 *
	 * @return einen Zeiger auf sich selbst, damit das Queue'n von Anweisungen möglich ist.
	 */
	MemoryCacheUnit resetCache();

	/**
	 * Gibt eine neue Instanz des internen memorys zurück.
	 *
	 * @return eine bel. Collection mit den, im Memory befindlichen Elementen
	 */
	Collection<T> duplicateMemory();

	/**
	 * Beschreibt, ob ein bestimmtes Element im cache befindet.
	 *
	 * @param t das Objekt, für welches man wissen möchte, ob es sich im Cache befindet
	 * @return True, wenn sich das Objekt im cache befindet, sonst false
	 */
	boolean containedInCache(T t);

	/**
	 * Beschreibt, ob ein bestimmtes Element im memory befindet.
	 *
	 * @param t das Objekt, für welches man wissen möchte, ob es sich im memory befindet
	 * @return True, wenn sich das Objekt im memory befindet, sonst false
	 */
	boolean containedInMemory(T t);

	/**
	 * Other than {@link #size()}, this method returns the current size of the cache.
	 *
	 * @return the size of the cache
	 */
	int cacheSize();

	/**
	 * This method returns the current size of the memory
	 *
	 * @return the size of the memory
	 */
	int size();

	/**
	 * Adds an element to this MemoryCacheUnit.
	 * It adds the given Element to the Memory, so youd have to call {@link #resetCache()} to add it to the cache afterwards.
	 *
	 * @param t The element, that should be added
	 */
	void add(T t);

	void addAll(Collection<T> collection);
}
