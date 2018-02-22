package com.github.thorbenkuck.keller.utility;

import java.util.Scanner;

public class Strings {

	public static boolean isInteger(String s) {
		return isInteger(s, 10);
	}

	public static boolean isInteger(String s, int radix) {
		Scanner sc = new Scanner(s.trim());
		if (! sc.hasNextInt(radix)) return false;
		// we know it starts with a valid int, now make sure
		// there's nothing left!
		sc.nextInt(radix);
		return ! sc.hasNext();
	}

}
