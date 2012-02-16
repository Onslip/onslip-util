
package com.onslip.util;

import java.util.*;

public abstract class StringUtils {
    public static String leftAlign(String str, int len, char c) {
	char[] buffer = new char[len];
	Arrays.fill(buffer, c);
        int slen = Math.min(len, str.length());
	str.getChars(0, slen, buffer, 0);
	return new String(buffer);
    }

    public static String rightAlign(String str, int len, char c) {
	char[] buffer = new char[len];
	Arrays.fill(buffer, c);
        int slen = Math.min(len, str.length());
	str.getChars(0, slen, buffer, len - slen);
	return new String(buffer);
    }

    public static String trim(String str, char c) {
        int start = 0, end = str.length();

        while (start < end && str.charAt(start) == c) {
            ++start;
        }

        while (end > start && str.charAt(end - 1) == c) {
            --end;
        }

        return str.substring(start, end);
    }
}