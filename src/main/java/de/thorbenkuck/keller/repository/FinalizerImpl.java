package de.thorbenkuck.keller.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class FinalizerImpl<T> implements Finalizer<T> {

	private RepositoryInternals repositoryInternals;
	private ActionStack<T> actionStack;

	public FinalizerImpl(RepositoryInternals repositoryInternals, ActionStack<T> actionStack) {
		this.repositoryInternals = repositoryInternals;
		this.actionStack = actionStack;
	}

	@Override
	public T getFirst() {
		Stream stream = getFilteredStream();
		Optional<Object> optional = stream.findFirst();
		if(optional.isPresent()) {
			return (T) optional.get();
		} else {
			actionStack.getIfNotPresent().forEach(Runnable::run);
			return actionStack.getNullObject();
		}
	}

	@Override
	public T getAny() {
		return null;
	}

	@Override
	public Collection<T> getAll() {
		return null;
	}

	@Override
	public T run(Consumer<T> consumer) {
		return null;
	}

	@Override
	public T run(Runnable runnable) {
		return null;
	}

	private Stream getFilteredStream() {
		return repositoryInternals.stream()
				.filter(o -> o.getClass().equals(actionStack.getClass()))
				.filter(o -> actionStack.getPredicateList().stream()
						.filter(predicate -> predicate.test((T) o))
						.count() == actionStack.getPredicateList().size());
	}
}
