
package com.onslip.util;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Timer;
import java.util.TimerTask;

public abstract class IOUtils {
    public static Charset latin1 = Charset.forName("ISO-8859-1");
    public static Charset utf8   = Charset.forName("UTF-8");

    private static final Timer timer = new Timer(IOUtils.class.getSimpleName(), true);

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

    /**
     * Helper method to copy an InputStream to an OutputStream, with timeout and stop byte support.
     *
     * @param is           The InputStream to read from.
     * @param os           The OutputStream to write to.
     * @param timeout      Time-out, in milliseconds.
     * @param len          The maximum number of bytes to copy.
     * @param stopChars    An array of stop bytes. If any of the bytes in this array is found, copying stops.
     * @return             false if a time-out occured, else true.
     * @throws IOException On I/O errors.
     */
    public static boolean copyStream(InputStream is, OutputStream os, long timeout, long len, byte[] stopChars)
            throws IOException {
        final long expires = System.currentTimeMillis() + timeout;

        if (is instanceof PipedInputStream) { // PipedInputStream#available() does not detect if pipe has been closed.
            final InputStream wrapped = is;

            is = new InputStream() {
                @Override public int available() throws IOException {
                    int rc = wrapped.available();

                    return rc == 0 ? 1 /* Force a (potentially) blocking call to read() */: rc;
                }

                @Override public int read() throws IOException {
                    final Thread[] thread = { Thread.currentThread() };

                    try {
                        if (wrapped.available() == 0) {
                            timer.schedule(new TimerTask() {
                                @Override public void run() {
                                    synchronized (thread) {
                                        if (thread[0] != null) {
                                            thread[0].interrupt();
                                        }
                                    }
                                }
                            }, Math.max(0, expires - System.currentTimeMillis()));
                        }

                        return wrapped.read();
                    }
                    finally {
                        synchronized (thread) {
                            thread[0] = null;
                            Thread.interrupted(); // Clear interrupted flag, if set
                        }
                    }
                }
            };
        }

        long readBytes = 0;
        byte[] buffer = new byte[1];

        loop: while (readBytes < len) {
            int read;

            try {
                read = is.available() > 0 ? is.read(buffer) : 0;
            }
            catch (InterruptedIOException ex) {
                read = 0;
            }

            if (read == 1) {
                os.write(buffer);
                readBytes++;

                for (byte stopChar : stopChars) {
                    if (buffer[0] == stopChar) {
                        break loop;
                    }
                }
            }
            else if (read < 0) {
                break; // EOF
            }
            else if (System.currentTimeMillis() > expires) {
                return false; // Timeout
            }
            else {
                try {
                    Thread.sleep(10);
                }
                catch (InterruptedException ignored) {
                    throw new InterruptedIOException();
                }
            }
        }

        return true;
    }
}
