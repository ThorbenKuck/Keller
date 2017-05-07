package de.thorbenkuck.keller.datatypes.interfaces;

import de.thorbenkuck.keller.implementation.QueuedMemoryCacheUnit;

import java.io.Serializable;
import java.util.Iterator;

public interface MemoryCacheUnit<T> extends Iterable<T>, Iterator<T> {
	/**
	 * Diese Methode setzt den Cache zurück. Alle jemals gespeicherten und nicht wieder entfernten Elemente werden damit in den Cache gesetzt
	 *
	 * @return einen Zeiger auf sich selbst, damit das Queue'n von Anweisungen möglich ist.
	 */
	QueuedMemoryCacheUnit resetCache();

	/**
	 * Fügt ein Element dem Speicher hinzu. Es ist dabei auch möglich, QueuedMemoryCacheUnit's in den Speicher zu schreiben.
	 *
	 * @param t das Objekt, welches dem Speicher hinzugefügt werden soll.
	 * @return einen Zeiger auf sich selbst, damit das Queue'n von Anweisungen möglich ist.
	 */
	QueuedMemoryCacheUnit add(T t);

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
}
