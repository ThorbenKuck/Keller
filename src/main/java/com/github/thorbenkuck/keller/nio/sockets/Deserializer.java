package com.github.thorbenkuck.keller.nio.sockets;

import java.util.function.Function;

public final class Deserializer {

	private Function<String, Object> deserializer = string -> string;

	public final Object getDeSerializedContent(String message) {
		try {
			return deserializer.apply(message);
		} catch (IllegalStateException e) {
			e.printStackTrace();
			return null;
		}
	}

	public final void setDeserializer(Function<String, Object> function) {
		this.deserializer = function;
	}

}
