package de.thorbenkuck.tests.keller;

import com.github.thorbenkuck.keller.collection.MemoryCacheUnit;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class MemoryCacheUnitTest {

	@Test
	public void testCreation() {
		MemoryCacheUnit<TestObject> memoryCacheUnit = MemoryCacheUnit.queue();
		memoryCacheUnit = MemoryCacheUnit.stack();
		memoryCacheUnit = MemoryCacheUnit.synchronizedQueue();
		memoryCacheUnit = MemoryCacheUnit.synchronizedStack();
	}

	@Test
	public void testRunQueue() {
		TestObject testObject = new TestObject();
		MemoryCacheUnit<TestObject> memoryCacheUnit = MemoryCacheUnit.queue();
		memoryCacheUnit.add(testObject);
		memoryCacheUnit.resetCache();

		assertTrue(memoryCacheUnit.containedInCache(testObject));
		assertTrue(memoryCacheUnit.containedInMemory(testObject));

		for(TestObject testObject1 : memoryCacheUnit) {
			testObject1.setValue(1);
		}

		assertFalse(memoryCacheUnit.containedInCache(testObject));
		assertTrue(memoryCacheUnit.containedInMemory(testObject));

		memoryCacheUnit.resetCache();

		assertTrue(memoryCacheUnit.containedInCache(testObject));
		assertTrue(memoryCacheUnit.containedInMemory(testObject));
	}

	@Test
	public void testRunQueueSynchronized() {
		TestObject testObject = new TestObject();
		MemoryCacheUnit<TestObject> memoryCacheUnit = MemoryCacheUnit.synchronizedQueue();
		memoryCacheUnit.add(testObject);
		memoryCacheUnit.resetCache();

		assertTrue(memoryCacheUnit.containedInCache(testObject));
		assertTrue(memoryCacheUnit.containedInMemory(testObject));

		for(TestObject testObject1 : memoryCacheUnit) {
			testObject1.setValue(1);
		}

		assertFalse(memoryCacheUnit.containedInCache(testObject));
		assertTrue(memoryCacheUnit.containedInMemory(testObject));

		memoryCacheUnit.resetCache();

		assertTrue(memoryCacheUnit.containedInCache(testObject));
		assertTrue(memoryCacheUnit.containedInMemory(testObject));
	}

	@Test
	public void testRunStack() {
		TestObject testObject = new TestObject();
		MemoryCacheUnit<TestObject> memoryCacheUnit = MemoryCacheUnit.stack();
		memoryCacheUnit.add(testObject);
		memoryCacheUnit.resetCache();

		assertTrue(memoryCacheUnit.containedInCache(testObject));
		assertTrue(memoryCacheUnit.containedInMemory(testObject));

		for(TestObject testObject1 : memoryCacheUnit) {
			testObject1.setValue(1);
		}

		assertFalse(memoryCacheUnit.containedInCache(testObject));
		assertTrue(memoryCacheUnit.containedInMemory(testObject));

		memoryCacheUnit.resetCache();

		assertTrue(memoryCacheUnit.containedInCache(testObject));
		assertTrue(memoryCacheUnit.containedInMemory(testObject));
	}

	@Test
	public void testRunStackSynchronized() {
		TestObject testObject = new TestObject();
		MemoryCacheUnit<TestObject> memoryCacheUnit = MemoryCacheUnit.synchronizedStack();
		memoryCacheUnit.add(testObject);
		memoryCacheUnit.resetCache();

		assertTrue(memoryCacheUnit.containedInCache(testObject));
		assertTrue(memoryCacheUnit.containedInMemory(testObject));

		for(TestObject testObject1 : memoryCacheUnit) {
			testObject1.setValue(1);
		}

		assertFalse(memoryCacheUnit.containedInCache(testObject));
		assertTrue(memoryCacheUnit.containedInMemory(testObject));

		memoryCacheUnit.resetCache();

		assertTrue(memoryCacheUnit.containedInCache(testObject));
		assertTrue(memoryCacheUnit.containedInMemory(testObject));
	}

}
