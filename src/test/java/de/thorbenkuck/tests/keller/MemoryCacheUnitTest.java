package de.thorbenkuck.tests.keller;

import de.thorbenkuck.keller.implementation.collection.MemoryCacheUnit;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class MemoryCacheUnitTest {

	@Test
	public void testCreation() {
		MemoryCacheUnit<TestObject> memoryCacheUnit = MemoryCacheUnit.unifiedCreation();
	}

	@Test
	public void testRun() {
		TestObject testObject = new TestObject();
		MemoryCacheUnit<TestObject> memoryCacheUnit = MemoryCacheUnit.unifiedCreation();
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
