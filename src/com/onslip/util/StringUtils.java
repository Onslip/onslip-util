
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

    public static String join(Object[] array, String delim) {
        StringBuilder sb = new StringBuilder();

        for (Object o : array) {
            if (sb.length() != 0) {
                sb.append(delim);
            }

            sb.append(o.toString());
        }

        return sb.toString();
    }

    public static String escapeJava(String str) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < str.length(); ++i) {
            char c = str.charAt(i);

            switch (c) {
                case '\b': sb.append("\\b"); break;
                case '\t': sb.append("\\t"); break;
                case '\n': sb.append("\\n"); break;
                case '\f': sb.append("\\f"); break;
                case '\r': sb.append("\\r"); break;
                case '\"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;

                default:
                    if (Character.isISOControl(c)) {
                        sb.append(String.format("\\u%04X", (int) c));
                    }
                    else {
                        sb.append(c);
                    }
                    break;
            }
        }

        return sb.toString();
    }
}
