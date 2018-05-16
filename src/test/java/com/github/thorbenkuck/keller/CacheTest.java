package com.github.thorbenkuck.keller;

import com.github.thorbenkuck.keller.annotations.Testing;
import com.github.thorbenkuck.keller.cache.*;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

@Testing(Cache.class)
public class CacheTest {

	@Test
	public void addCacheAddition() {

		TestObject toTest = new TestObject();

		Cache cache = Cache.create();
		TestCacheObserver testCacheObserver = new TestCacheObserver();
		cache.addCacheObserver(TestObject.class, testCacheObserver);

		cache.addNew(toTest);
		toTest.setValue(10);
		cache.addAndOverride(toTest);
		cache.remove(toTest.getClass());
	}

}

class TestCacheObserver implements CacheObserver<TestObject> {

	private boolean successfull = false;

	TestCacheObserver() {
	}

	boolean successfull() {
		return successfull;
	}

	@Override
	public void newEntry(TestObject newEntryEvent, Cache cache) {
		System.out.println("Neues Object! " + newEntryEvent.getValue());
	}

	@Override
	public void updatedEntry(TestObject updatedEntryEvent, Cache cache) {
		System.out.println("Updated Object! " + updatedEntryEvent.getValue());
	}

	@Override
	public void deletedEntry(Class<TestObject> t, Cache cache) {
		System.out.println("oh, gelöscht.. " + t);
		cache.removeCacheObserver(TestObject.class, this);
	}
}
