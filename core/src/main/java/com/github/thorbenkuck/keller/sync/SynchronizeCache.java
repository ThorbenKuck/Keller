package com.github.thorbenkuck.keller.sync;

final class SynchronizeCache {

	static Synchronize empty;

	static Synchronize getEmpty() {
		checkNull();
		return empty;
	}

	static boolean isEmpty(Awaiting awaiting) {
		return empty != null && awaiting == empty;
	}

	private static synchronized void checkNull() {
		if(empty != null){
			return;
		}
		empty = new EmptySynchronize();
	}

}
