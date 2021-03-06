package com.github.thorbenkuck.keller;

import com.github.thorbenkuck.keller.annotations.Testing;
import com.github.thorbenkuck.keller.command.Enforcer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@Testing({Enforcer.class})
public class CommandEnforcerTest {

	@Test
	public void basicTest() {
		TestObject testObject = new TestObject();
		Enforcer<TestObject> enforcer = Enforcer.create();

		enforcer.addCommand(testObject1 -> testObject1.setValue(testObject1.getValue() + 1));
		enforcer.addCommand(testObject1 -> testObject1.setValue(testObject1.getValue() * 2));
		// Erwartet = (0+1) * 2 = 2

		enforcer.run(testObject);
		assertEquals(testObject.getValue(), 2);
	}

	@Test
	public void inverseBasicTest() {
		TestObject testObject = new TestObject();
		Enforcer<TestObject> enforcer = Enforcer.create();

		enforcer.addCommand(testObject1 -> testObject1.setValue(testObject1.getValue() * 2));
		enforcer.addCommand(testObject1 -> testObject1.setValue(testObject1.getValue() + 1));
		// Erwartet = (0*2) + 1 = 1

		enforcer.run(testObject);
		assertEquals(testObject.getValue(), 1);
	}

	@Test
	public void testCleanUp() {
		final TestObject testObject = new TestObject();
		Enforcer<TestObject> enforcer = Enforcer.create();
		enforcer.setDoOnFinish(() -> testObject.setValue(12));

		enforcer.addCommand(testObject1 -> testObject1.setValue(testObject1.getValue() + 1));
		// Erwartet = 0 + 1 = 1
		// Danach reset auf 12

		enforcer.run(testObject);
		assertEquals(testObject.getValue(), 12);
	}

	@Test
	public void testCreation() {
		Enforcer<TestObject> enforcer = Enforcer.create();
	}

}