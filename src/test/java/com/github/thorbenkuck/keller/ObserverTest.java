package com.github.thorbenkuck.keller;

import com.github.thorbenkuck.keller.datatypes.observers.AbstractGenericObservable;
import com.github.thorbenkuck.keller.datatypes.observers.GenericObserver;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ObserverTest {

	@Test
	public void testObserver() {
		ObservableAbstract observable = new ObservableAbstract();

		GenericObserver<TestValue> observer = GenericObserver.of(TestValue.class, ((observingValue, abstractGenericObservable) -> {
			System.out.println("Observer changed");
			assertEquals(observingValue.count, 42);
		}));

		observable.addObserver(observer);
		observable.fire();
	}

}


class ObservableAbstract extends AbstractGenericObservable<TestValue> {

	private TestValue observingValue = new TestValue();

	void fire() {
		observingValue.count = 42;
		setChanged();
		notifyObservers(observingValue);
	}

}