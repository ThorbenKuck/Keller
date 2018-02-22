package com.github.thorbenkuck.keller.collection;

import java.util.Collection;
import java.util.Iterator;

public interface MemoryCacheUnit<T> extends Iterable<T>, Iterator<T> {

	static <T> MemoryCacheUnit<T> unifiedCreation() {
		return new QueuedMemoryCacheUnit<>();
	}

//	static <T> MemoryCacheUnit<T> of(Collection<T> copy) {
//
//	}

	/**
	 * Diese Methode setzt den Cache zurück. Alle jemals gespeicherten und nicht wieder entfernten Elemente werden damit in den Cache gesetzt
	 *
	 * @return einen Zeiger auf sich selbst, damit das Queue'n von Anweisungen möglich ist.
	 */
	QueuedMemoryCacheUnit resetCache();

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
	 * */
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
}
