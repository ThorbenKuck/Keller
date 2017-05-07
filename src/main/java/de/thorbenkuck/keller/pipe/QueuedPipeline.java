package de.thorbenkuck.keller.pipe;

import java.util.LinkedList;
import java.util.Queue;

public class QueuedPipeline<T> extends AbstractPipeline<T, Queue<PipelineService<T>>> implements Pipeline<T> {

	public QueuedPipeline() {
		super(new LinkedList<>());
	}

	@Override
	public void addLast(PipelineService<T> pipelineService) {
		synchronized (coreLock) {
			synchronized (core) {
				core.add(pipelineService);
			}
		}
	}

	@Override
	public void addFirst(PipelineService<T> pipelineService) {
		Queue<PipelineService<T>> newCore = new LinkedList<>();
		newCore.add(pipelineService);
		synchronized (coreLock) {
			synchronized (core) {
				newCore.addAll(core);
				core.clear();
				core.addAll(newCore);
			}
		}
	}

	@Override
	public void remove(PipelineService<T> pipelineService) {
		synchronized (coreLock) {
			synchronized (core) {
				core.remove(pipelineService);
			}
		}
	}
}
