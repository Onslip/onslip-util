
package com.onslip.util;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

public abstract class IOUtils {
    public static Charset latin1 = Charset.forName("ISO-8859-1");
    public static Charset utf8   = Charset.forName("UTF-8");

    public static ThreadLocal<CharsetDecoder> utf8Decoder = new ThreadLocal<CharsetDecoder>() {
        @Override public CharsetDecoder initialValue() {
            return utf8.newDecoder();
        }
    };

    public static ThreadLocal<CharsetEncoder> latin1Encoder = new ThreadLocal<CharsetEncoder>() {
        @Override public CharsetEncoder initialValue() {
            return latin1.newEncoder();
        }
    };

    public static String readLatin1(InputStream is, int characters)
        throws IOException {
        return new String(readBytes(is, characters), latin1);
    }

    public static String readUTF8(InputStream is, int characters)
        throws IOException {
        return new String(readBytes(is, characters), utf8);
    }

    public static String readLatin1OrUTF8(InputStream is, int characters)
        throws IOException {
        return readLatin1OrUTF8(readBytes(is, characters));
    }

    public static String readLatin1OrUTF8(byte[] bytes) {
        try {
            if (bytes.length >= 3 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
                // Skip BOM, parse UTF-8 stricly
                return utf8Decoder.get().decode(ByteBuffer.wrap(bytes, 3, bytes.length - 3)).toString();
            }
        }
        catch (CharacterCodingException ignored) {
            // Fall back to Latin-1
        }

        return new String(bytes, latin1);
    }

    public static byte[] writeLatin1OrUTF8(String string) {
        if (latin1Encoder.get().canEncode(string)) {
            return string.getBytes(latin1);
        }
        else {
            return ("\ufeff" + string).getBytes(utf8);
        }
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
