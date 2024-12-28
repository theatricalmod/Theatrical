package dev.imabad.theatrical.client.sound;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class PacketInputStream extends InputStream {
    private final BlockingQueue<byte[]> audioQueue = new LinkedBlockingQueue<>();
    private ByteArrayInputStream currentStream = null;

    // Flag indicating whether this stream is open or closed
    private volatile boolean closed = false;

    // How long to wait (in milliseconds) for new packets before giving up
    private final long timeoutMillis;

    public PacketInputStream(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    @Override
    public int read() throws IOException {
        // If stream is closed and we've drained all data, return EOF
        if (closed && currentStream == null) {
            return -1;
        }

        // If currentStream is exhausted or null, try to fetch a new packet
        while ((currentStream == null || currentStream.available() == 0) && !closed) {
            // Poll the queue with timeout
            try {
                byte[] nextPacket = audioQueue.poll(timeoutMillis, TimeUnit.MILLISECONDS);

                if (nextPacket == null) {
                    // Timed out waiting for data; decide what you want to do
                    // Option 1: Return -1 (EOF-like behavior)
                    // Option 2: Return 0 (indicates no data now, but might have later)
                    // Option 3: Keep looping until closed

                    // This example will return -1 to indicate "no more data available now."
                    return -1;
                } else {
                    currentStream = new ByteArrayInputStream(nextPacket);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while waiting for audio data", e);
            }
        }

        // If still no currentStream, we're likely closed
        if (currentStream == null) {
            return -1;
        }

        // Read one byte
        int result = currentStream.read();

        // If we are done reading that packet, set currentStream to null
        if (currentStream.available() == 0) {
            currentStream = null;
        }
        return result;
    }

    // Overriding for efficiency
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (closed && currentStream == null) {
            return -1;
        }

        int totalBytesRead = 0;

        while (totalBytesRead < len) {
            if (currentStream == null || currentStream.available() == 0) {
                // Try to get more data
                byte[] nextPacket;
                try {
                    nextPacket = audioQueue.poll(timeoutMillis, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted while waiting for audio data", e);
                }

                if (nextPacket == null) {
                    // No data arrived in time
                    if (totalBytesRead == 0) {
                        return -1; // Indicate no more data if we haven't read anything
                    } else {
                        break;     // We have read something, so exit loop with partial read
                    }
                } else {
                    currentStream = new ByteArrayInputStream(nextPacket);
                }
            }

            int bytesCanRead = Math.min(len - totalBytesRead, currentStream.available());
            int bytesReadNow = currentStream.read(b, off + totalBytesRead, bytesCanRead);
            totalBytesRead += bytesReadNow;

            if (currentStream.available() == 0) {
                currentStream = null; // We used up this packet
            }
        }

        return totalBytesRead > 0 ? totalBytesRead : -1;
    }

    /**
     * Write a new packet of audio data to the stream's queue.
     */
    public void writePacket(byte[] packet) throws IOException {
        if (closed) {
            throw new IOException("Cannot write to a closed PacketInputStream.");
        }
        audioQueue.offer(packet);
    }

    /**
     * Close this input stream, indicating no more data will arrive.
     */
    @Override
    public void close() {
        closed = true;
        // Optionally clear the queue if you want to discard leftover data
        audioQueue.clear();
    }

    /**
     * Returns whether the stream has been closed.
     */
    public boolean isClosed() {
        return closed;
    }
}
