package com.github.thorbenkuck.keller.di;

import java.util.function.Consumer;

public interface PostCreationStrategy extends Consumer<Object> {

	static PostCreationStrategy binding(InstantiateDispatcher dispatcher) {
		return new BindingPostCreationStrategy(dispatcher);
	}

	static PostCreationStrategy bindAs(InstantiateDispatcher dispatcher) {
		return new BindAsPostCreationStrategy(dispatcher);
	}

	static PostCreationStrategy cache(InstantiateDispatcher dispatcher) {
		return new CachePostCreationStrategy(dispatcher);
	}

	static void applyTo(InstantiateDispatcher dispatcher) {
		dispatcher.addPostCreationConsumer(binding(dispatcher));
		dispatcher.addPostCreationConsumer(bindAs(dispatcher));
		dispatcher.addPostCreationConsumer(cache(dispatcher));
	}
}
