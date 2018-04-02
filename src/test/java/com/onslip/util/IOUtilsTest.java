package com.onslip.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Timer;
import java.util.TimerTask;
import org.junit.*;
import static org.junit.Assert.*;

public class IOUtilsTest {
    private static Timer timer = new Timer();
    private static byte[] noBytes = new byte[0];

    @Test public void copyStreamTimeout() throws IOException {
        final PipedInputStream       pis = new PipedInputStream();
        final PipedOutputStream      pos = new PipedOutputStream(pis);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Timeout, no data
        long now = System.currentTimeMillis();
        boolean rc = IOUtils.copyStream(pis, baos, 1000, 5, noBytes);
        assertArrayEquals(new byte[0], baos.toByteArray());
        assertEquals(false, rc);
        assertTrue(System.currentTimeMillis() - now >= 1000, "More than one second passed");
        assertTrue(System.currentTimeMillis() - now < 2000, "Less than two seconds passed");
        baos.reset();

        // Delayed data, more than what we need
        timer.schedule(new TimerTask() {
            @Override public void run() {
                try {
                    pos.write(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }, 500);

        now = System.currentTimeMillis();
        rc = IOUtils.copyStream(pis, baos, 2000, 5, noBytes);
        assertArrayEquals(new byte[] { 1, 2, 3, 4, 5 }, baos.toByteArray());
        assertEquals(true, rc);
        assertTrue(System.currentTimeMillis() - now >= 500, "More than half a second passed");
        assertTrue(System.currentTimeMillis() - now < 3000, "Less than three seconds passed");
        baos.reset();

        // Wait for stop byte
        now = System.currentTimeMillis();
        rc = IOUtils.copyStream(pis, baos, 2000, 5, new byte[] { 9, 7 });
        assertArrayEquals(new byte[] { 6, 7 }, baos.toByteArray());
        assertEquals(true, rc);
        assertTrue(System.currentTimeMillis() - now < 500, "Less than half a second passed");
        baos.reset();

        // Handle forced end-of stream/cancellation
        timer.schedule(new TimerTask() {
            @Override public void run() {
                try {
                    pis.close();
                    pos.close();
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }, 500);

        now = System.currentTimeMillis();
        rc = IOUtils.copyStream(pis, baos, 10000, 10, noBytes);
        assertArrayEquals(new byte[] { 8, 9, 10 }, baos.toByteArray());
        assertEquals(false, rc);
        assertTrue(System.currentTimeMillis() - now >= 500, "More than half a second passed");
        assertTrue(System.currentTimeMillis() - now < 1000, "Less than one second passed");
        baos.reset();

        // Throw on actual IO errors (both streams are closed at this point)
        boolean throwOnErrors = false;

        try {
            IOUtils.copyStream(pis, baos, 10000, 10, noBytes);
        }
        catch (IOException ex) {
            throwOnErrors = true;
        }

        assertTrue(throwOnErrors);
    }
}
