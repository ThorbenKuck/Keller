package com.github.thorbenkuck.keller.datatypes.runnables;

import com.github.thorbenkuck.keller.sync.Awaiting;
import com.github.thorbenkuck.keller.sync.Synchronize;

/**
 * AwaitingRunnable kombiniert die Freiheit eines Runnable, also z.B. die Nutzung in einem ExecutorService
 * und kombiniert diese mit der Funktionialität darauf zu warten, dass dieser fertig wird.
 * <p>
 * Um StackOverflows zu vermeiden, wird hierfür ein interner CountDownLatch genutzt.
 * Dieser kann entweder default mit einem minimalen delay gesetzt werden (Default Konsturktor) oder
 * mit einem eigenem Delay initialisiert werden.
 * Wenn die implementierende Klasse fertig ist, sollte diese finish aufrufen.
 * Mit finish wird der CountDownLatch dann ausgelöst.
 * <p>
 * Um auf diese Klasse zu warten kann einfach:
 * <code>
 * *.getHanger().await(); genutzt werden.
 * </code>
 * Der Thread, welcher die await Methode aufruft, wartet dann so lange, bis der Thread, welcher die AwaitingRunnable extended, finish() aufruft.
 */
public abstract class AwaitingRunnable implements Runnable {

	private final Synchronize synchronize = Synchronize.createDefault();

	/**
	 * Der Default-Konstruktor initialisiert den CountDownLatch mit einer erwarteten anzahl an Aufrufen von 1
	 */
	protected AwaitingRunnable () {
	}

	protected abstract void execute();

	@Override
	public void run() {
		execute();
		finish();
	}

	/**
	 * The Synchronization mechanism for awaiting the finish of this Runnable.
	 *
	 * @return die Awaiting instance die gesetzt ist
	 * @see Awaiting
	 * @see Synchronize
	 */
	public Awaiting synchronization() {
		return synchronize;
	}

	/**
	 * Diese Methode muss von der Klasse, welche diese abstrakte Klasse implementiert aufgerufen werden.
	 * Sie signalisiert das Ende der, in einem anderen Thread ausgeführten Prozedur.
	 *
	 * WICHTIG! Wird diese Klasse NICHT aufgerufen, so warten andere Prozeduren ewig!
	 */
	protected void finish() {
		synchronize.goOn();
	}
}
