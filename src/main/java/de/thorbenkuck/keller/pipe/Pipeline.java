package de.thorbenkuck.keller.pipe;

import de.thorbenkuck.keller.datatypes.interfaces.Acceptor;

public interface Pipeline<T> {
	void addLast(Acceptor<T> acceptor);

	void addFirst(Acceptor<T> acceptor);

	void remove(Acceptor<T> acceptor);

	void call(T e);

	void setMode(PipelineModes pipelineMode);
}
