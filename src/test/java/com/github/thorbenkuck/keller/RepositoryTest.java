package com.github.thorbenkuck.keller;

import com.github.thorbenkuck.keller.annotations.Testing;
import com.github.thorbenkuck.keller.repository.Repository;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collection;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

@Testing(Repository.class)
public class RepositoryTest {

	@Ignore
	@Test
	public void generalTest() {
		Repository repository = Repository.instantiate();

		repository.put(new TestObject(0));
		repository.put(new TestObject(1));
		repository.put(new TestObject(2));

		System.out.println(repository.access(TestObject.class).getAll());

		TestObject returnValue = repository.access(TestObject.class)
				.withRequirement()
				.objectFulfills(Objects::nonNull)
				.and()
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
