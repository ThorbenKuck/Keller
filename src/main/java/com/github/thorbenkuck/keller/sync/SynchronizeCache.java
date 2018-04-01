package com.github.thorbenkuck.keller.sync;

class SynchronizeCache {

	static Synchronize empty;

	static Synchronize getEmpty() {
		checkNull();
		return empty;
	}

	private static synchronized void checkNull() {
		if(empty != null){
			return;
		}
		empty = new EmptySynchronize();
	}

}
