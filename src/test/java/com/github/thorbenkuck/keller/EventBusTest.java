package com.github.thorbenkuck.keller;

import com.github.thorbenkuck.keller.event.*;
import org.junit.Ignore;
import org.junit.Test;

public class EventBusTest {

	@Ignore
	@Test
	public void test() throws InterruptedException {
		EventBus eventBus = EventBus.create();
		DispatcherStrategy.parallel(eventBus);
		TestObject testObject = new TestObject();
		eventBus.register(testObject);
		eventBus.register(new TestObject2());
		eventBus.register(new DeadEventHandler());
		eventBus.register(new SecondTestObject());


		eventBus.post(new TestEvent());
		Thread.sleep(100);
		eventBus.unregister(testObject);
		eventBus.post(new TestEvent());
		Thread.sleep(100);
	}

	private final class TestObject {
		@Hook
		public SecondTestEvent handle(TestEvent testEvent) {
			System.out.println("[TestObject," + Thread.currentThread().getName() + "]: " + testEvent.count);
			SecondTestEvent event = new SecondTestEvent();
			event.message = "Queried " + testEvent.count;
			return event;
		}

		@Override
		public String toString() {
			return "TestObject{Hook}";
		}
	}

	private final class TestObject2 {
		@Listen
		public void handle(TestEvent testEvent) {
			System.out.println("[TestObject2," + Thread.currentThread().getName() + "]: UH! Found one!");
		}

		@Override
		public String toString() {
			return "TestObject{Hook}";
		}
	}

	private final class SecondTestObject {
		@Listen
		public void handle(SecondTestEvent event) {
			System.out.println("[SecondTestObject," + Thread.currentThread().getName() + "]: " + event.message);
		}

		@Override
		public String toString() {
			return "SecondTestObject{Listener}";
		}
	}

	private final class DeadEventHandler {
		@Listen
		public void handle(DeadEvent deadEvent) {
			System.out.println("No Handler found for " + deadEvent.getEvent().getClass());
		}

		@Override
		public String toString() {
			return "DeadEventHandler{Listener}";
		}
	}

	private final class TestEvent {
		int count = 0;

		@Override
		public String toString() {
			return "TestEvent";
		}
	}

	private final class SecondTestEvent {
		String message = "";

		@Override
		public String toString() {
			return "SecondTestEvent";
		}
	}

}
