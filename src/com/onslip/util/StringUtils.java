
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

    public static String indent(int level) {
        char[] result = new char[level];
        Arrays.fill(result, ' ');
        return new String(result);
    }

    public static String indent(int level, String string) {
        StringBuilder sb = new StringBuilder();

        for (String line : string.split("\n")) {
            if (sb.length() > 0) {
                sb.append("\n");
            }

            sb.append(String.format("%s%s", indent(level), line));
        }

        return sb.toString();
    }

    public static String wrap(int width, String string) {
        StringBuilder sb = new StringBuilder();

        for (String line : string.split("\n")) {
            if (sb.length() > 0) {
                sb.append("\n");
            }

            int col = 0;

            StringTokenizer st = new StringTokenizer(line);

            while (st.hasMoreTokens()) {
                String word = st.nextToken();

                if (col + word.length() > width) {
                    sb.append("\n");
                    col = 0;
                }

                sb.append(word).append(" ");
                col += word.length() + 1;
            }
        }

        return sb.toString();
    }
}
