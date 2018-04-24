package com.github.thorbenkuck.keller;

import com.github.thorbenkuck.keller.event.EventBus;
import com.github.thorbenkuck.keller.event.Hook;
import org.junit.Ignore;
import org.junit.Test;

public class EventBusTest {

	@Ignore
	@Test
	public void test() {
		EventBus eventBus = EventBus.create();
		TestObject object = new TestObject();
		eventBus.register(object);
		eventBus.register(new SecondTestObject());
		eventBus.post(new TestEvent());
		eventBus.unregister(object);
		eventBus.post(new TestEvent());
	}

	private final class TestObject {
		@Hook
		public SecondTestEvent handle(TestEvent testEvent) {
			System.out.println("[TestObject]" + testEvent.count);
			SecondTestEvent event = new SecondTestEvent();
			event.message = "Queried " + testEvent.count;
			return event;
		}
	}

	private final class SecondTestObject {
		@Hook
		public void handle(SecondTestEvent event) {
			System.out.println("[SecondTestObject]: " + event.message);
		}
	}

	private final class TestEvent {
		int count = 0;
	}

	private final class SecondTestEvent {
		String message = "";
	}

}
