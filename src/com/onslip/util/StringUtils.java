
package com.onslip.util;

import java.util.*;

public abstract class StringUtils {
    public static String leftAlign(String str, int len, char c) {
	char[] buffer = new char[len];
	Arrays.fill(buffer, c);
	str.getChars(0, str.length(), buffer, 0);
	return new String(buffer);
    }

    public static String rightAlign(String str, int len, char c) {
	char[] buffer = new char[len];
	Arrays.fill(buffer, c);
	str.getChars(0, str.length(), buffer, len - str.length());
	return new String(buffer);
    }

}