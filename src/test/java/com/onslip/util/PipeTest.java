package com.onslip.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import static org.junit.jupiter.api.Assertions.*;

public class PipeTest {
    @Test public void testUnConnected() {
        final Pipe.Source src = new Pipe.Source();
        final Pipe.Sink   dst = new Pipe.Sink();

        assertThrows(IOException.class, new Executable() { public void execute() throws Exception { src.read(); } });
        assertThrows(IOException.class, new Executable() { public void execute() throws Exception { dst.write(1); } });
    }

    @Test public void testConnected() throws IOException {
        Pipe.Source src = new Pipe.Source();
        Pipe.Sink   dst = new Pipe.Sink(src);

        dst.write(1);
        assertEquals(1, src.read());

        dst = new Pipe.Sink();
        src = new Pipe.Source(dst);

        dst.write(2);
        assertEquals(2, src.read());

        src = new Pipe.Source();
        dst = new Pipe.Sink();
        src.connect(dst);

        dst.write(3);
        assertEquals(3, src.read());

        src = new Pipe.Source();
        dst = new Pipe.Sink();
        dst.connect(src);

        dst.write(4);
        assertEquals(4, src.read());

        final Pipe.Source s = src;
        final Pipe.Sink   d = dst;

        assertThrows(IOException.class, new Executable() { public void execute() throws Exception { s.connect(d); } });
        assertThrows(IOException.class, new Executable() { public void execute() throws Exception { d.connect(s); } });
    }

    @Test public void testWrite() throws IOException {
        Pipe.Source src = new Pipe.Source();
        Pipe.Sink   dst = new Pipe.Sink(src);

        dst.write(1);
        dst.write(new byte[] { 2, 3 });
        dst.write(new byte[] { 2, 3, 4, 5, 6, 7 }, 2, 3);

        assertEquals(1, src.read());
        assertEquals(2, src.read());
        assertArrayEquals(new byte[] { 3, 4, 5, 6 }, IOUtils.readBytes(src, 4));

        assertTrue(src.available() == 0);

        byte[] destroyed = new byte[] { 7, 8 };
        dst.write(destroyed);
        Arrays.fill(destroyed, (byte) 9);
        dst.write(destroyed, 1, 1);
        Arrays.fill(destroyed, (byte) 0);
        dst.close();

        assertTrue(src.available() > 0);
        byte[] last = new byte[3];
        assertEquals(3, src.read(last));;
        assertArrayEquals(new byte[] { 7, 8, 9}, last);

        assertTrue(src.available() == 0);
        assertEquals(-1, src.read());
        assertEquals(-1, src.read(last));
        assertEquals(-1, src.read(last, 0, 1));
    }

    @Test public void testWakeUp() throws IOException {
        final Pipe.Source src = new Pipe.Source();
        final Pipe.Sink   dst = new Pipe.Sink(src);

        new Timer().schedule(new TimerTask() {
            @Override public void run() {
                try {
                    dst.write(1);
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }, 150);

        long start = System.currentTimeMillis();
        assertEquals(1, src.read());
        assertTrue(System.currentTimeMillis() - start >= 100, "More than 100 ms passed");
        assertTrue(System.currentTimeMillis() - start < 500, "Less than 500 ms passed");
    }

    @Test public void testEOF() throws IOException {
        final Pipe.Source src = new Pipe.Source();
        final Pipe.Sink   dst = new Pipe.Sink(src);

        dst.write(2);

        new Timer().schedule(new TimerTask() {
            @Override public void run() {
                try {
                    dst.close();
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }, 150);

        long start = System.currentTimeMillis();
        byte[] buffer = new byte[4];
        assertTrue(src.available() > 0);
        assertEquals(1, src.read(buffer));
        assertArrayEquals(new byte[] { 2, 0, 0, 0 }, buffer);
        assertEquals(-1, src.read(buffer));
        assertTrue(System.currentTimeMillis() - start >= 100, "More than 100 ms passed");
        assertTrue(System.currentTimeMillis() - start < 500, "Less than 500 ms passed");

        assertTrue(src.available() == 0);
        src.close();

        assertThrows(IOException.class, new Executable() { public void execute() throws Exception { src.read(); } });
        assertThrows(IOException.class, new Executable() { public void execute() throws Exception { src.available(); } });
    }

    @Test public void testCancel() throws IOException {
        final Pipe.Source src = new Pipe.Source();
        final Pipe.Sink   dst = new Pipe.Sink(src);

        new Timer().schedule(new TimerTask() {
            @Override public void run() {
                try {
                    src.close();
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }, 150);

        long start = System.currentTimeMillis();
        assertThrows(IOException.class, new Executable() { public void execute() throws Exception { src.read(); } });
        assertThrows(IOException.class, new Executable() { public void execute() throws Exception { src.available(); } });
        assertTrue(System.currentTimeMillis() - start >= 100, "More than 100 ms passed");
        assertTrue(System.currentTimeMillis() - start < 500, "Less than 500 ms passed");
    }
}
