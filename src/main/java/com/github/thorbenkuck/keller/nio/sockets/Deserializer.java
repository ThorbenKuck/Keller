package com.github.thorbenkuck.keller.nio.sockets;

import java.util.function.Function;

public class Deserializer {

	private Function<String, Object> deserializer = string -> string;

	public Object getDeSerializedContent(String message) {
		try {
			return deserializer.apply(message);
		} catch (IllegalStateException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void setDeserializer(Function<String, Object> function) {
		this.deserializer = function;
	}

}
