package com.github.thorbenkuck.keller.nio.sockets;

import java.util.List;

class BestFitChoosingStrategy implements ChoosingStrategy {

	private final boolean lowest;

	BestFitChoosingStrategy(boolean lowest) {
		this.lowest = lowest;
	}

	/**
	 * Applies this function to the given argument.
	 *
	 * @param selectorChannels the function argument
	 * @return the function result
	 */
	@Override
	public SelectorChannel apply(List<SelectorChannel> selectorChannels) {
		if(lowest) {
			return findLowest(selectorChannels);
		} else {
			return findHighest(selectorChannels);
		}
	}

	private SelectorChannel findLowest(List<SelectorChannel> selectorChannels) {
		SelectorChannel lowest = null;
		for(SelectorChannel channel : selectorChannels) {
			if(lowest == null) {
				lowest = channel;
			} else {
				if(channel.getWorkload() < lowest.getWorkload()) {
					lowest = channel;
				}
			}
		}

		return lowest;
	}

	private SelectorChannel findHighest(List<SelectorChannel> selectorChannels) {
		SelectorChannel highest = null;
		for(SelectorChannel channel : selectorChannels) {
			if(highest == null) {
				highest = channel;
			} else {
				if(channel.getWorkload() > highest.getWorkload()) {
					highest = channel;
				}
			}
		}

		return highest;
	}
}
