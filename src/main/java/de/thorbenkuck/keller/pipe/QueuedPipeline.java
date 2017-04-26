package de.thorbenkuck.keller.pipe;

import de.thorbenkuck.keller.datatypes.interfaces.Acceptor;

import java.util.LinkedList;
import java.util.Queue;

public class QueuedPipeline<T> extends AbstractPipeline<T, Queue<Acceptor<T>>> implements Pipeline<T> {

	public QueuedPipeline() {
		super(new LinkedList<>());
	}

	@Override
	public void addLast(Acceptor<T> acceptor) {
		if(!acceptableToCore(acceptor)) {
			return;
		}

		synchronized (coreLock) {
			synchronized (core) {
				core.add(acceptor);
			}
		}
	}

	@Override
	public void addFirst(Acceptor<T> acceptor) {
		if(!acceptableToCore(acceptor)) {
			return;
		}

		Queue<Acceptor<T>> newCore = new LinkedList<>();
		newCore.add(acceptor);
		synchronized (coreLock) {
			synchronized (core) {
				newCore.addAll(core);
				core.clear();
				core.addAll(newCore);
			}
		}
	}

	@Override
	public void remove(Acceptor<T> acceptor) {
		synchronized (coreLock) {
			synchronized (core) {
				core.remove(acceptor);
			}
		}
	}
}
