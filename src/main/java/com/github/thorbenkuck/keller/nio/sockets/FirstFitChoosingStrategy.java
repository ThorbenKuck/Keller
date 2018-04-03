package com.github.thorbenkuck.keller.nio.sockets;

import java.util.List;

class FirstFitChoosingStrategy implements ChoosingStrategy {
	/**
	 * Applies this function to the given argument.
	 *
	 * @param selectorChannels the function argument
	 * @return the function result
	 */
	@Override
	public SelectorChannel apply(List<SelectorChannel> selectorChannels) {
		for(SelectorChannel socketChannels : selectorChannels) {
			if(socketChannels.isOpen()) {
				return socketChannels;
			}
		}
		return null;
	}
}
