package com.github.thorbenkuck.keller.nio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.function.Function;

public class JavaSerializer implements Function<Object, String> {
	/**
	 * Applies this function to the given argument.
	 *
	 * @param o the function argument
	 * @return the function result
	 */
	@Override
	public String apply(Object o) {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(o);
			oos.flush();
		} catch (final IOException e) {
			throw new IllegalStateException(e);
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				baos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return Base64.getEncoder().encodeToString(baos.toByteArray());
	}
}
