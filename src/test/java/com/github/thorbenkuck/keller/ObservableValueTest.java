package com.github.thorbenkuck.keller;

import com.github.thorbenkuck.keller.annotations.Testing;
import com.github.thorbenkuck.keller.datatypes.observers.ObservableValue;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@Testing(ObservableValue.class)
public class ObservableValueTest {

	@Test
	public void run() {

		ObservableValue<TestValue> observableValue = ObservableValue.of(new TestValue());

		observableValue.addObserver(((testValue, source) -> {
			System.out.println("Received Value");
			assertEquals(testValue.count, 42);
		}));

		TestValue testValue = new TestValue();
		testValue.count = 42;

		observableValue.set(testValue);
	}

}
