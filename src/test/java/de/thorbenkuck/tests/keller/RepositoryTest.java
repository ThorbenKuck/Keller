package de.thorbenkuck.tests.keller;

import de.thorbenkuck.keller.repository.Repository;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class RepositoryTest {

	@Test
	public void generalTest() {
		Repository repository = Repository.instantiate();

		repository.put(new TestObject(0));
		repository.put(new TestObject(1));
		repository.put(new TestObject(2));

		TestObject returnValue = repository.access(TestObject.class)
				.withRequirement()
				.objectFulfills(testObject -> testObject.getValue() > 0)
				.and()
				.objectDoesNotMeet(testObject -> testObject.getValue() == 2)
				.then()
				.ifNotPresent()
				.throwException(new IllegalStateException("TestObject"))
				.otherwise()
				.getFirst();

		assertEquals(1, returnValue.getValue());

		returnValue = repository.access(TestObject.class)
				.getAny();

		System.out.println(returnValue.getValue());

		repository.access(TestObject.class)
				.ifNotPresent()
				.throwException(new IllegalStateException("TestObject"))
				.now();

		Collection<TestObject> testObjectCollection = repository.access(TestObject.class)
				.getAll();

		assertEquals(3, testObjectCollection.size());
	}

}
