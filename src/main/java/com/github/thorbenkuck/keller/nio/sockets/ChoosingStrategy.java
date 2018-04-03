package com.github.thorbenkuck.keller.nio.sockets;

import java.util.List;
import java.util.function.Function;

public interface ChoosingStrategy extends Function<List<SelectorChannel>, SelectorChannel> {

	static ChoosingStrategy firstFit() {
		return new FirstFitChoosingStrategy();
	}

	static ChoosingStrategy lowestWorkloadFirst() {
		return new BestFitChoosingStrategy(true);
	}

	static ChoosingStrategy highestWorkloadFirst() {
		return new BestFitChoosingStrategy(false);
	}

}
