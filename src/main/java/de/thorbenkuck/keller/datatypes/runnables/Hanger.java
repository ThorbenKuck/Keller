package de.thorbenkuck.keller.datatypes.runnables;

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;

/**
 * Der Hanger ist eigentlich nur eine verschleierungs-Klasse, welche eine Instanz eines CountDownLatch aus einem Thread
 * isoliert.
 * Dafür stellt er lediglich die Methode "await" zur verfügung, um, mittels der {@link AwaitingRunnable} Klasse, ein
 * sicheres warten auf Runnable's zu ermöglichen.
 * <p>
 * Dadurch kann auch auf Runnable's in einem ExecutorService gewartet werden.
 * Diese Klasse st notwendig, damit ein {@link CountDownLatch} losgelöst von der Instanz von mehreren Prozeduren genutzt
 * werden kann. Ansonsten ist diese Klasse nicht notwendig
 */
public class Hanger  implements Serializable {

	private CountDownLatch countDownLatch;

	@Override
	public String toString() {
		return "Hanger{" +
				"countDownLatch=" + countDownLatch +
				'}';
	}

	/**
	 * Der Hanger kann nicht außerhalb dieses Packages instantiiert werden, sofern er nicht extended wird.
	 * Ansonsten wird er lediglich von dem AwaitingRunnable instantiiert und zurück gegeben.
	 *
	 * @param countDownLatch der CountDownLatch, auf welchen gewartet werden soll
	 */
	protected Hanger (CountDownLatch countDownLatch) {
		this.countDownLatch = countDownLatch;
	}

	/**
	 * Diese Methode wartet darauf, dass der, im Konstruktor übergebene CountDownLatch fertig wird.
	 *
	 * @throws InterruptedException Sollte die ursprungs AwaitingRunnable von irgendetwas unterbrochen werden, so wird
	 *                              diese Information durch gereicht.
	 */
	public void await () throws InterruptedException {
		countDownLatch.await();
	}
}

