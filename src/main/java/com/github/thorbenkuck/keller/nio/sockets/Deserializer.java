package com.github.thorbenkuck.keller.nio.sockets;

import java.util.function.Function;

public class Deserializer {

	private Function<String, Object> deserializer = string -> string;

	public Object getDeSerializedContent(String message) {
		return deserializer.apply(message);
	}

	public void setDeserializer(Function<String, Object> function) {
		this.deserializer = function;
	}

}
