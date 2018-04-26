package com.github.thorbenkuck.keller.nio.sockets;

import java.util.List;
import java.util.function.Function;

public interface ChoosingStrategy extends Function<List<SelectorChannel>, SelectorChannel> {

	static ChoosingStrategy firstFit() {
		return new ChoosingStrategyFirstFit();
	}

	static ChoosingStrategy lowestWorkloadFirst() {
		return new ChoosingStrategyBestFit(true);
	}

	static ChoosingStrategy highestWorkloadFirst() {
		return new ChoosingStrategyBestFit(false);
	}

}
