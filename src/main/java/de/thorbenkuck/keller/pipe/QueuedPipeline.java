package de.thorbenkuck.keller.pipe;

import de.thorbenkuck.keller.datatypes.interfaces.PipelineHandler;

import java.util.LinkedList;
import java.util.Queue;

public class QueuedPipeline<T> extends AbstractPipeline<T, Queue<PipelineHandler<T>>> implements Pipeline<T> {

	public QueuedPipeline() {
		super(new LinkedList<>());
	}

	@Override
	public void addLast(PipelineHandler<T> pipelineHandler) {
		if(!acceptableToCore(pipelineHandler)) {
			return;
		}

		synchronized (coreLock) {
			synchronized (core) {
				core.add(pipelineHandler);
			}
		}
	}

	@Override
	public void addFirst(PipelineHandler<T> pipelineHandler) {
		if(!acceptableToCore(pipelineHandler)) {
			return;
		}

		Queue<PipelineHandler<T>> newCore = new LinkedList<>();
		newCore.add(pipelineHandler);
		synchronized (coreLock) {
			synchronized (core) {
				newCore.addAll(core);
				core.clear();
				core.addAll(newCore);
			}
		}
	}

	@Override
	public void remove(PipelineHandler<T> pipelineHandler) {
		synchronized (coreLock) {
			synchronized (core) {
				core.remove(pipelineHandler);
			}
		}
	}
}
