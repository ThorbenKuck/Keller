import de.thorbenkuck.keller.cache.*;
import org.junit.Test;

import java.util.Observable;
import java.util.function.Predicate;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class CacheTest {

	@Test
	public void addCacheAddition() {

		Object toTest = new Object();

		Cache cache = Cache.create();
		TestCacheObserver testCacheObserver = new TestCacheObserver(o ->
				o.getClass().equals(NewEntryEvent.class)
						&& ((NewEntryEvent) o).getObject().getClass().equals(Object.class)
		);
		cache.addCacheObserver(testCacheObserver);

		cache.addAndOverride(toTest);
		assertTrue(testCacheObserver.successfull());
		cache.addAndOverride(toTest);
		assertFalse(testCacheObserver.successfull());
	}

}

class TestCacheObserver extends AbstractCacheObserver {

	private boolean successfull = false;
	private Predicate<Object> objectPredicate;

	TestCacheObserver(Predicate<Object> objectPredicate) {
		this.objectPredicate = objectPredicate;
	}

	boolean successfull() {
		return successfull;
	}

	@Override
	public void newEntry(NewEntryEvent newEntryEvent, Observable observable) {
		successfull = objectPredicate.test(newEntryEvent);
	}

	@Override
	public void updatedEntry(UpdatedEntryEvent updatedEntryEvent, Observable observable) {
		successfull = objectPredicate.test(updatedEntryEvent);
	}

	@Override
	public void deletedEntry(DeletedEntryEvent deletedEntryEvent, Observable observable) {
		successfull = objectPredicate.test(deletedEntryEvent);
	}
}
