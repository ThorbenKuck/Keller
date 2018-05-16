package com.github.thorbenkuck.keller.repository;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

final class RepositoryInternals {

	private final List<Object> objectList = new ArrayList<>();
	private CountDownLatch countDownLatch = new CountDownLatch(0);

	public final boolean clear() {
		try {
			synchronize();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		objectList.clear();
		return isEmpty();
	}

	public final boolean isEmpty() {
		try {
			synchronize();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		return objectList.isEmpty();
	}

	public final void lock() {
		try {
			countDownLatch.await();

			synchronized (objectList) {
				countDownLatch = new CountDownLatch(1);
			}
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	public final void unlock() {
		countDownLatch.countDown();
	}

	public final void synchronize() throws InterruptedException {
		countDownLatch.await();
	}

	public final boolean contains(final Object o) {
		try {
			synchronize();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		return objectList.contains(o);
	}

	public final void add(final Object o) {
		try {
			synchronize();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		objectList.add(o);
	}

	public final List<Object> copyInternalsAsList() {
		try {
			synchronize();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		return new ArrayList<>(objectList);
	}

	public final Queue<Object> copyInternalsAsQueue() {
		try {
			synchronize();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		return new LinkedList<>(objectList);
	}

	public final Stream<Object> stream() {
		try {
			synchronize();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		return objectList.stream();
	}

}
