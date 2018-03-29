package com.github.thorbenkuck.keller.nio.sockets;

import java.util.function.Function;

public class Deserializer {

	private Function<String, Object> deserializer;

	public Object getDeSerializedContent(Message message) {
		return deserializer.apply(message.getContent());
	}

	public void setDeserializer(Function<String, Object> function) {
		this.deserializer = function;
	}

}
