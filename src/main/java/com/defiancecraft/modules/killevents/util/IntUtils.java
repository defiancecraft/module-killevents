package com.defiancecraft.modules.killevents.util;

public class IntUtils {

	/**
	 * Gets the suffix for an integer (i.e. 'st', 'nd', 'rd', 'th')
	 * @param i Integer to get suffix for
	 * @return Suffix
	 */
	public static String getSuffix(int i) {
		return i > 10 && i < 14 ? "th" : // 11th, 12th, 13th
			   i % 10 == 1 ? "st" : // *1st
			   i % 10 == 2 ? "nd" : // *2nd
			   i % 10 == 3 ? "rd" : // *3rd
				             "th";  // Anything else
	}
	
	/**
	 * Gets an integer and its suffix as a string.
	 * 
	 * @param i Integer to get suffix of
	 * @see #getSuffix(int)
	 * @return Integer + suffix
	 */
	public static String getWithSuffix(int i) {
		return i + getSuffix(i);
	}
	
	/**
	 * Compares two `int` values numerically against natural order (i.e. descending).
	 * The value returned is identical to what would be returned by:
	 * <code>Integer.compare(b, a)</code>
	 * 
	 * @param a Integer a
	 * @param b Integer b
	 * @see Integer#compare(int, int)
	 * @return the value 0 if a == b; a value less than 0 if b < a; and a value greater than 0 if b > a
	 */
	public static int compareReversed(int a, int b) {
		return Integer.compare(b, a);
	}
	
}
