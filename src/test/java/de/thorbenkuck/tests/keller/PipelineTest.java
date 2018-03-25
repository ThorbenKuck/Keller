package de.thorbenkuck.tests.keller;

import com.github.thorbenkuck.keller.pipe.Pipeline;
import com.github.thorbenkuck.keller.datatypes.QueuedPipeline;
import org.junit.Test;

import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;

public class PipelineTest {

	@Test
	public void addTest() {
		Pipeline<TestObject> pipeline = new QueuedPipeline<>();
		pipeline.addLast((Consumer<TestObject>) testObject -> testObject.setValue(testObject.getValue() * 2));
		pipeline.addLast((Consumer<TestObject>) testObject -> testObject.setValue(testObject.getValue() + 1));

		TestObject testObject = new TestObject();
		pipeline.apply(testObject);

		assertEquals(testObject.getValue(), 1);
	}

	@Test
	public void addTestInverse() {
		Pipeline<TestObject> pipeline = new QueuedPipeline<>();
		pipeline.addLast((Consumer<TestObject>) testObject -> testObject.setValue(testObject.getValue() + 1));
		pipeline.addLast((Consumer<TestObject>) testObject -> testObject.setValue(testObject.getValue() * 2));

		TestObject testObject = new TestObject();
		pipeline.apply(testObject);

		assertEquals(testObject.getValue(), 2);
	}

	@Test
	public void addTestWithAddFirst() {
		Pipeline<TestObject> pipeline = new QueuedPipeline<>();
		pipeline.addLast((Consumer<TestObject>) testObject -> testObject.setValue(testObject.getValue() * 2));
		pipeline.addLast((Consumer<TestObject>) testObject -> testObject.setValue(testObject.getValue() + 1));
		pipeline.addFirst((Consumer<TestObject>) testObject -> testObject.setValue(testObject.getValue() + 1));

		TestObject testObject = new TestObject();
		pipeline.apply(testObject);

		assertEquals(testObject.getValue(), 3);
	}

}

