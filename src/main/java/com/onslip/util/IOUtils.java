
package com.onslip.util;

import java.io.*;

public abstract class IOUtils {
    public static java.nio.charset.Charset latin1 = java.nio.charset.Charset.forName("ISO-8859-1");
    public static java.nio.charset.Charset utf8 = java.nio.charset.Charset.forName("UTF-8");

    public static String readLatin1(InputStream is, int characters)
        throws IOException {
        return new String(readBytes(is, characters), latin1);
    }

    public static byte[] readBytes(InputStream is, int bytes)
        throws IOException {
        return readBytes(is, bytes, 0);
    }

    public static byte[] readBytes(InputStream is, int bytes, int timeout)
        throws IOException {
        byte[] buf = new byte[bytes];

        readFully(is, buf, timeout);
        return buf;
    }

    public static void readFully(InputStream is, byte[] buf)
                throws IOException {
        readFully(is, buf, 0);
    }

    public static void readFully(InputStream is, byte[] buf, int timeout)
        throws IOException {
        int pos = 0, cnt;
        long expires = System.currentTimeMillis() + (timeout > 0 ? timeout : Integer.MAX_VALUE);

        while (pos < buf.length && (cnt = is.read(buf, pos, buf.length - pos)) >= 0 && System.currentTimeMillis() < expires) {
            pos += cnt;
        }

        if (pos < buf.length) {
            throw new EOFException("EOF while reading " + buf.length + " characters; received only " + pos);
        }
    }

    public static byte[] readFully(InputStream is)
        throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copyStream(is, baos);
        return baos.toByteArray();
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

    public static long copyStream(InputStream is, OutputStream os)
      throws IOException {
      byte buffer[] = new byte[8192];
      long length = 0;

      int bytesRead;

      while ((bytesRead = is.read(buffer)) != -1) {
	os.write(buffer, 0, bytesRead);
        length += bytesRead;
      }

      os.flush();
      return length;
    }

    public static long copyStream(InputStream is, boolean closeIS, OutputStream os, boolean closeOS)
        throws IOException {
        try {
            return copyStream(is, os);
        }
        finally {
            if (closeIS) try { is.close(); } catch (IOException ignored) {}
            if (closeOS) try { os.close(); } catch (IOException ignored) {}
        }
    }
}