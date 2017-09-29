package de.thorbenkuck.keller.repository;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

public class RepositoryInternals {

	private final List<Object> objectList = new ArrayList<>();
	private CountDownLatch countDownLatch = new CountDownLatch(0);

	public boolean clear() {
		try {
			synchronize();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		objectList.clear();
		return isEmpty();
	}

	public boolean isEmpty() {
		try {
			synchronize();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return objectList.isEmpty();
	}

	public void lock() {
		try {
			countDownLatch.await();

			synchronized (objectList) {
				countDownLatch = new CountDownLatch(1);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void unlock() {
		countDownLatch.countDown();
	}

	public void synchronize() throws InterruptedException {
		countDownLatch.await();
	}

	public boolean contains(Object o) {
		try {
			synchronize();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return objectList.contains(o);
	}

	public void add(Object o) {
		try {
			synchronize();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		objectList.add(o);
	}

	public List<Object> copyInternalsAsList() {
		try {
			synchronize();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return new ArrayList<>(objectList);
	}

	public Queue<Object> copyInternalsAsQueue() {
		try {
			synchronize();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return new LinkedList<>(objectList);
	}

	public Stream<Object> stream() {
		try {
			synchronize();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return objectList.stream();
	}

}
