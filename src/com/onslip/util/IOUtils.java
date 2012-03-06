
package com.onslip.util;

import java.io.*;

public abstract class IOUtils {
    public static java.nio.charset.Charset latin1 = java.nio.charset.Charset.forName("ISO-8859-1");

    public static String readLatin1(InputStream is, int characters)
        throws IOException {
        return new String(readBytes(is, characters), latin1);
    }


    public static byte[] readBytes(InputStream is, int bytes)
        throws IOException {
        byte[] buf = new byte[bytes];

        readFully(is, buf);
        return buf;
    }

    public static void readFully(InputStream is, byte[] buf)
        throws IOException {
        int pos = 0, cnt;

        while (pos < buf.length && (cnt = is.read(buf, pos, buf.length - pos)) > 0) {
            pos += cnt;
        }

        if (pos < buf.length) {
            throw new EOFException("EOF while reading " + buf.length + " characters");
        }
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

    public static void copyStream(InputStream is, OutputStream os)
      throws IOException {
      byte buffer[] = new byte[8192];

      int bytesRead;

      while ((bytesRead = is.read(buffer)) != -1) {
	os.write(buffer, 0, bytesRead);
      }

      os.flush();
    }
}
