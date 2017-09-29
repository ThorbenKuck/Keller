package de.thorbenkuck.keller.datatypes.interfaces;

/**
 * <p>
 * Dieses Interface bietet eine Möglichkeit, Actionen zu queue'n.
 * Die Reihenfolge dabei sieht wie folgt aus:
 * </p>
 * <p>doBefore() -> doAction() -> doAfter()</p>
 * <p>
 *     Es ist notwendig, die Aktion doAction() zu definieren. Diese repräsentiert dabei, die zentrale Aktion.
 *     Alternativ können die Methoden doBefore() und doAfter() implementiert werden. Diese können, müssen aber nicht implementiert werden.
 *     Default machen diese Methoden nichts, was bedeutet, dass die Aufrufe ohne Probleme durchgeführt werden können.
 * </p>
 */
@FunctionalInterface
public interface QueuedAction {

	/**
	 * Diese Methode beschreibt, was vor der Aktion passieren soll.
	 */
	default void doBefore() {
	}

	/**
	 * Diese Methode beschreibt, was während der Aktion passieren soll.
	 */
	void doAction();

	/**
	 * Diese Methode beschreibt, was nache der Aktion passieren soll.
	 */
	default void doAfter() {
	}

	/**
	 * Diese Methode ruft die notwendigen methoden auf. Dadurch muss das nicht immer wieder geschrieben werden.
	 */
	static void call(QueuedAction action) {
		action.doBefore();
		action.doAction();
		action.doAfter();
	}
}