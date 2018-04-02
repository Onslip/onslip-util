package com.onslip.util;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class Pipe {
    public static class Source extends InputStream {
        private Sink sink;
        private LinkedBlockingQueue<byte[]> queuedChunks = new LinkedBlockingQueue<byte[]>();
        private InputStream currentInputStream = new ByteArrayInputStream(new byte[0]);
        private final Object currentInputStreamMutex = new Object();
        private volatile boolean endOfFile;
        private volatile boolean closed;

        public Source() {
        }

        public Source(Sink s) throws IOException {
            connect(s);
        }

        public synchronized void connect(Sink s) throws IOException {
            synchronized (s) {
                if (sink != null || s.source != null) {
                    throw new IOException("Pipe already connected");
                }

                sink     = s;
                s.source = this;
            }
        }

        @Override public synchronized void close() throws IOException {
            if (sink != null) {
                sink.close(); // Writing no longer allowed
            }

            closed = true;
            receive(new byte[0] /* EOF */); // Wake up blocked reader (if any)
        }

        @Override public int read() throws IOException {
            return currentInputStream(true).read();
		}

        @Override public int available() throws IOException {
            return currentInputStream(false).available();
        }

        private InputStream currentInputStream(boolean blockIfEmpty) throws IOException {
            if (sink == null) {
                throw new IOException("Pipe not connected");
            }
            else if (closed) {
                throw new IOException("Pipe closed");
            }

            synchronized (currentInputStreamMutex) {
                if (!endOfFile && currentInputStream.available() == 0 && (blockIfEmpty || !queuedChunks.isEmpty())) {
                    try {
                        ArrayList<byte[]> chunks = new ArrayList<byte[]>(queuedChunks.size());

                        do {
                            byte[] chunk = queuedChunks.take();
                            endOfFile = endOfFile || chunk.length == 0;
                            chunks.add(chunk);
                        } while (!queuedChunks.isEmpty());

                        currentInputStream = new ByteArrayInputStream(ByteUtils.concat(chunks));
                    }
                    catch (InterruptedException ex) {
                        throw new InterruptedIOException();
                    }
                }

                if (closed) {
                    throw new IOException("Pipe closed"); // We were closed while blocked in take()
                }
                else {
                    return currentInputStream;
                }
            }
        }

        private void receive(byte[] chunk) throws IOException {
            try {
                queuedChunks.put(chunk);
            }
            catch (InterruptedException ex) { // put() should never block so this should never really happen
                throw new InterruptedIOException();
            }
        }
    }

    public static class Sink extends OutputStream {
        private Source source;
        private volatile boolean closed;

        public Sink() {
        }

        public Sink(Source s) throws IOException {
            connect(s);
        }

        public void connect(Source s) throws IOException {
            s.connect(this);
        }

        @Override public void write(int b) throws IOException {
            send(new byte[] { (byte) b });
        }

        @Override public void write(byte b[], int off, int len) throws IOException {
            send(Arrays.copyOfRange(b, off, off + len));
        }

        @Override public void write(byte b[]) throws IOException {
            send(Arrays.copyOf(b, b.length));
        }

        @Override public synchronized void close() throws IOException {
            if (!closed && source != null) {
                source.receive(new byte[0] /* EOF */);
            }

            closed = true;
        }

        private void send(byte[] b) throws IOException {
            if (source == null) {
                throw new IOException("Pipe not connected");
            }
            else if (closed) {
                throw new IOException("Pipe closed");
            }

            if (b.length > 0) {
                source.receive(b);
            }
        }
    }
}
