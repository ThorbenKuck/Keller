package com.github.thorbenkuck.keller.pipe;

import com.github.thorbenkuck.keller.TestObject;
import com.github.thorbenkuck.keller.annotations.Testing;
import org.junit.Test;

import java.util.function.Consumer;

import static org.junit.Assert.*;

@Testing(Pipeline.class)
public class PipelineTest {
	@Test
	public void unifiedCreation() throws Exception {
		// Arrange
		Pipeline<TestHandle> pipeline;

		// Act
		pipeline = Pipeline.unifiedCreation();

		// Assert
		assertEquals(pipeline.getClass(), NativePipeline.class);
	}

	@Test
	public void addLast() throws Exception {
		// Arrange
		Pipeline<TestHandle> pipeline = Pipeline.unifiedCreation();

		// Act
		pipeline.addLast(object -> {});

		// Assert
		assertEquals(1, pipeline.size());
	}

	@Test
	public void addLastFunction() throws Exception {
		// Arrange
		Pipeline<TestHandle> pipeline = Pipeline.unifiedCreation();

		// Act
		pipeline.addLast(object -> object);

		// Assert
		assertEquals(1, pipeline.size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void addLastNull() throws Exception {
		// Arrange
		Pipeline<TestHandle> pipeline = Pipeline.unifiedCreation();

		// Act
		pipeline.addLast((Consumer<TestHandle>) null);

		// Assert
		fail();
	}

	@Test
	public void apply() {
		// Arrange
		Pipeline<TestHandle> pipeline = Pipeline.unifiedCreation();
		pipeline.addLast(TestHandle::increment);
		pipeline.addLast(TestHandle::increment);
		pipeline.addLast((Consumer<TestHandle>) testHandle -> testHandle.count = testHandle.count * 2);
		pipeline.addLast(TestHandle::increment);
		pipeline.addLast((Consumer<TestHandle>) testHandle -> testHandle.count = testHandle.count / 2);

		// Act
		TestHandle input = new TestHandle();
		TestHandle testHandle = pipeline.apply(input);

		// Assert
		assertEquals(3, testHandle.count);
		assertEquals(input, testHandle);
	}

	@Test
	public void addLast1() throws Exception {
		// Arrange

		// Act

		// Assert
	}

	@Test
	public void addFirst() throws Exception {
		// Arrange

		// Act

		// Assert
	}

	@Test
	public void addFirst1() throws Exception {
		// Arrange

		// Act

		// Assert
	}

	@Test
	public void remove() throws Exception {
		// Arrange

		// Act

		// Assert
	}

	@Test
	public void remove1() throws Exception {
		// Arrange

		// Act

		// Assert
	}

	@Test
	public void count() throws Exception {
		// Arrange

		// Act

		// Assert
	}

	@Test
	public void clear() throws Exception {
		// Arrange

		// Act

		// Assert
	}

	@Test
	public void addTest() {
		Pipeline<TestObject> pipeline = new NativePipeline<>();
		pipeline.addLast((Consumer<TestObject>) testObject -> testObject.setValue(testObject.getValue() * 2));
		pipeline.addLast((Consumer<TestObject>) testObject -> testObject.setValue(testObject.getValue() + 1));

		TestObject testObject = new TestObject();
		pipeline.apply(testObject);

		assertEquals(testObject.getValue(), 1);
	}

	@Test
	public void addTestInverse() {
		Pipeline<TestObject> pipeline = new NativePipeline<>();
		pipeline.addLast((Consumer<TestObject>) testObject -> testObject.setValue(testObject.getValue() + 1));
		pipeline.addLast((Consumer<TestObject>) testObject -> testObject.setValue(testObject.getValue() * 2));

		TestObject testObject = new TestObject();
		pipeline.apply(testObject);

		assertEquals(testObject.getValue(), 2);
	}

	@Test
	public void addTestWithAddFirst() {
		Pipeline<TestObject> pipeline = new NativePipeline<>();
		pipeline.addLast((Consumer<TestObject>) testObject -> testObject.setValue(testObject.getValue() * 2));
		pipeline.addLast((Consumer<TestObject>) testObject -> testObject.setValue(testObject.getValue() + 1));
		pipeline.addFirst((Consumer<TestObject>) testObject -> testObject.setValue(testObject.getValue() + 1));

		TestObject testObject = new TestObject();
		pipeline.apply(testObject);

		assertEquals(testObject.getValue(), 3);
	}

	private class TestHandle {

		private int count = 1;

		public void increment() {
			++count;
		}

		public void decrement() {
			--count;
		}

	}

}