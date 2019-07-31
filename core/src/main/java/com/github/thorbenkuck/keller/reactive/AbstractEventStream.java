package com.github.thorbenkuck.keller.reactive;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractEventStream<T> implements WritableEventStream<T> {

	private final List<ConcreteSubscription<T>> subscriptions = new ArrayList<>();

	private void publish(T t) {
		List<ConcreteSubscription<T>> copy;

		synchronized (subscriptions) {
			copy = new ArrayList<>(subscriptions);
		}

		dispatch(copy, t);
		copy.clear();
		copy = null; // Just help the GC
	}

	protected abstract void dispatch(List<ConcreteSubscription<T>> subscriptions, T t);

	@Override
	public void push(T t) {
		publish(t);
	}

	@Override
	public Subscription subscribe(Subscriber<T> subscriber) {
		ConcreteSubscription<T> subscription = new InnerSubscription(subscriber);
		synchronized (subscriptions) {
			subscriptions.add(subscription);
		}
		return subscription;
	}

	private final class InnerSubscription implements ConcreteSubscription<T> {

		private final Subscriber<T> subscriber;
		private final List<Throwable> throwables = new ArrayList<>();
		private final Object lock = new Object();

		private InnerSubscription(Subscriber<T> subscriber) {
			this.subscriber = subscriber;
		}

		@Override
		public void notify(T t) {
			synchronized (lock) {
				if (!isCanceled()) {
					subscriber.accept(t);
				}
			}
		}

		@Override
		public boolean isCanceled() {
			synchronized (subscriptions) {
				return !subscriptions.contains(this);
			}
		}

		@Override
		public void cancel() {
			if (isCanceled()) {
				return;
			}
			synchronized (lock) {
				synchronized (subscriptions) {
					subscriptions.remove(this);
				}
			}
		}

		@Override
		public List<Throwable> getEncounteredErrors() {
			List<Throwable> toReturn;
			synchronized (throwables) {
				toReturn = new ArrayList<>(throwables);
			}
			throwables.clear();
			return toReturn;
		}

		@Override
		public boolean hasEncounteredErrors() {
			synchronized (throwables) {
				return !throwables.isEmpty();
			}
		}
	}
}
