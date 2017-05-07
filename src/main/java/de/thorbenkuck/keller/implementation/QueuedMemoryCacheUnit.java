package de.thorbenkuck.keller.implementation;

import de.thorbenkuck.keller.datatypes.interfaces.MemoryCacheUnit;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

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
	protected volatile int numberOfElements;

	/**
	 * Da die QueuedMemoryCacheUnit nicht selbst instanziiert werden soll, sondern erweitert, ist der Konstruktor protected.
	 * So wird verhindert, dass Prozesse, welche diese Nutzen, eine leere QueuedMemoryCacheUnit erhalten.
	 */
	protected QueuedMemoryCacheUnit() {
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
	 * Diese Methode setzt den Cache zurück. Alle jemals gespeicherten und nicht wieder entfernten Elemente werden damit in den Cache gesetzt
	 *
	 * @return einen Zeiger auf sich selbst, damit das Queue'n von Anweisungen möglich ist.
	 */
	@Override
	public final QueuedMemoryCacheUnit resetCache() {
		this.cache = new LinkedList<>(memory);
		return this;
	}

	/**
	 * Fügt ein Element dem Speicher hinzu. Es ist dabei auch möglich, QueuedMemoryCacheUnit's in den Speicher zu schreiben.
	 *
	 * @param t das Objekt, welches dem Speicher hinzugefügt werden soll.
	 * @return einen Zeiger auf sich selbst, damit das Queue'n von Anweisungen möglich ist.
	 */
	@Override
	public final QueuedMemoryCacheUnit add(T t) {
		memory.add(t);
		if (t instanceof QueuedMemoryCacheUnit) {
			numberOfElements = numberOfElements + ((QueuedMemoryCacheUnit) t).numberOfElements;
		} else {
			numberOfElements++;
		}
		return resetCache();
	}

	/**
	 * Bekomme das nächste Element, aus dem Cache. Dieser Aufruf ist äquivalent zu cache.remove() und verhält sich, wie das Entfernen eines Fifo-Elements
	 *
	 * @return das unterste Element der cache-queue
	 */
	@Override
	public final T next() {
		return cache.remove();
	}

	/**
	 * holt den Iterator aus dem cache.
	 *
	 * @return den Iterator des cache's
	 */
	@Override
	public final Iterator<T> iterator() {
		return cache.iterator();
	}

	/**
	 * Beschreibt, ob es noch ein weiteres Element im cache gibt, oder dieser leer ist.
	 *
	 * @return True, wenn der cache nicht leer ist, sonst false
	 */
	@Override
	public final boolean hasNext() {
		return cache.peek() != null;
	}

	/**
	 * Beschreibt, ob ein bestimmtes Element im cache befindet.
	 *
	 * @param t das Objekt, für welches man wissen möchte, ob es sich im Cache befindet
	 * @return True, wenn sich das Objekt im cache befindet, sonst false
	 */
	@Override
	public final boolean containedInCache(T t) {
		return cache.contains(t);
	}

	/**
	 * Beschreibt, ob ein bestimmtes Element im memory befindet.
	 *
	 * @param t das Objekt, für welches man wissen möchte, ob es sich im memory befindet
	 * @return True, wenn sich das Objekt im memory befindet, sonst false
	 * */
	@Override
	public final boolean containedInMemory(T t) {
		return memory.contains(t);
	}

}
