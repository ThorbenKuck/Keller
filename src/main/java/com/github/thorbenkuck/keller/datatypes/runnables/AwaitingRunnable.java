package com.github.thorbenkuck.keller.datatypes.runnables;

import java.util.concurrent.CountDownLatch;

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

	private CountDownLatch countDownLatch;

	/**
	 * Der Default-Konstruktor initialisiert den CountDownLatch mit einer erwarteten anzahl an Aufrufen von 1
	 */
	protected AwaitingRunnable () {
		this(1);
	}

	/**
	 * Dieser Konstruktor erwartet einen Integer wert, repräsentativ dafür, wie häufig die finish Methode aufgerufen werden muss,
	 * damit der Runnable als beendet gilt.
	 *
	 * ACHTUNG! Dieser Konstruktor ist ggf. fehler-Anfällig! Wenn dieser Konstruktor genutzt wird, verändert man die Mechanik des AwaitingRunnable!
	 * Wird zum beispiel eine 1 übergeben (default), so wird erwartet, dass genau 1 mal finish aufgerufen wird.
	 * Wird hingegen eine 2 übergeben, so muss finish 2 mal aufgerufen werden, bis alle wartenden Prozeduren released werden!
	 * summa summarum: Wird n übergeben muss n mal finish aufgerufen werden, ansonsten warten die anderen Prozeduren endlos!
	 *
	 * @param counts die Anzahl, wie häufig finish betätigt wird
	 */
	private AwaitingRunnable (int counts) {
		countDownLatch = new CountDownLatch(counts);
	}

	/**
	 * Der Hanger ist eine Klasse, die einen CountDownLatch aus einem Thread isoliert und ihn,
	 * auf die Methode await() reduziert, zur Verfügung stellt
	 *
	 * @return einen Neuen Hanger für jeden Methoden aufruf
	 */
	public Hanger getHanger() {
		return new Hanger(this.countDownLatch);
	}

	/**
	 * Diese Methode muss von der Klasse, welche diese abstrakte Klasse implementiert aufgerufen werden.
	 * Sie signalisiert das Ende der, in einem anderen Thread ausgeführten Prozedur.
	 *
	 * WICHTIG! Wird diese Klasse NICHT aufgerufen, so warten andere Prozeduren ewig!
	 * Der interne CountDownLatch existiert nämlich so lange weiter, wie es auch nur einen Hanger gibt, der wartet!
	 */
	protected void finish() {
		countDownLatch.countDown();
	}
}
