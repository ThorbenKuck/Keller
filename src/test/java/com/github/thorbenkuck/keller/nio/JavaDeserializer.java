package com.github.thorbenkuck.keller.nio;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Base64;
import java.util.function.Function;

public class JavaDeserializer implements Function<String, Object> {
	/**
	 * Applies this function to the given argument.
	 *
	 * @param s the function argument
	 * @return the function result
	 */
	@Override
	public Object apply(String s) {
		final Object o;
		try {
			byte[] data = Base64.getDecoder().decode(s.getBytes());
			final ObjectInputStream ois = new ObjectInputStream(
					new ByteArrayInputStream(data));
			o = ois.readObject();
			ois.close();
		} catch (final Throwable e) {
			throw new IllegalStateException("Error while reading the given serialized Object.. " +
					"\nGiven serialized Object: " + s, e);
		}

		if (o == null) {
			throw new IllegalStateException("Error while reading the given serialized Object .. " +
					"\nGiven serialized Object: " + s);
		}
		return o;
	}
}
