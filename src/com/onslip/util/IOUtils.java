
package com.onslip.util;

import java.io.*;

public abstract class IOUtils {
    public static java.nio.charset.Charset latin1 = java.nio.charset.Charset.forName("ISO-8859-1");

    public static String readLatin1(InputStream is, int characters)
        throws IOException {
        byte[] buf = new byte[characters];

        readFully(is, buf);
        return new String(buf, latin1);
    }

    public static void readFully(InputStream is, byte[] buf)
        throws IOException {
        int pos = 0, cnt;

        while ((cnt = is.read(buf, pos, buf.length - pos)) > 0) {
            pos += cnt;

            if (pos == buf.length) {
                return;
            }
        }

        throw new EOFException("EOF while reading " + buf.length + " characters");
    }

    public static String readUntilControl(PushbackInputStream is)
        throws IOException {
        StringBuffer sb = new StringBuffer();

        while (true) {
            char c = (char) is.read();

            if (Character.isISOControl(c)) {
                is.unread(c);
                break;
            }
            else {
                sb.append(c);
            }
        }

        return sb.toString();
    }
}