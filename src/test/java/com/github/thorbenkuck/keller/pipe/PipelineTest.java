package com.github.thorbenkuck.keller.pipe;

import com.github.thorbenkuck.keller.datatypes.QueuedPipeline;
import org.junit.Test;

import java.util.function.Consumer;

import static org.junit.Assert.*;

public class PipelineTest {
	@Test
	public void unifiedCreation() throws Exception {
		// Arrange
		Pipeline<TestHandle> pipeline;

		// Act
		pipeline = Pipeline.unifiedCreation();

		// Assert
		assertEquals(pipeline.getClass(), QueuedPipeline.class);
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

	@Test
	public void addLastNull() throws Exception {
		// Arrange
		Pipeline<TestHandle> pipeline = Pipeline.unifiedCreation();

		// Act
		pipeline.addLast((Consumer<TestHandle>) null);

		// Assert
		assertEquals(1, pipeline.size());
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