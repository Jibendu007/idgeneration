package com.volante.idgeneration.util;
public class IDGenerator {
    private static final long EPOCH = System.currentTimeMillis(); // Epoch time
    public static final long WORKER_ID_BITS = 13L; // Bits allocated for worker ID
    private static final long SEQUENCE_BITS = 10L; // Bits allocated for sequence number
    private static final long SEQUENCE_MASK = (1L << SEQUENCE_BITS) - 1L; // Sequence number mask
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS; // Timestamp left shift
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS; // Worker ID left shift
    private long workerId; // Worker ID for this machine
    private long lastTimestamp = -1L; // Last timestamp used
    private long sequence = 0L; // Sequence number
    public IDGenerator(long workerId) {
        // Ensure worker ID is within allowed range
        if (workerId > (1L << WORKER_ID_BITS) - 1L) {
            throw new IllegalArgumentException("Worker ID is larger than allowed range");
        }
        this.workerId = workerId;
    }
    public synchronized void setWorkerId(long workerId) {
        if (workerId > (1L << WORKER_ID_BITS) - 1L) {
            throw new IllegalArgumentException("Worker ID is larger than allowed range");
        }
        this.workerId = workerId;
    }
    public synchronized long generateID() {
        long timestamp = System.currentTimeMillis();
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backward. Refusing to generate ID");//just for safety
        }
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK; // Increment sequence number
            if (sequence == 0) { // Sequence overflow
                // Wait for next millisecond to generate unique ID
                while (timestamp <= lastTimestamp) {
                    try {
                        Thread.sleep(0);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } else {
            sequence = 0; // Reset sequence for new millisecond
        }
        lastTimestamp = timestamp;
        // Pack timestamp, worker ID, and sequence into long
        long id = (timestamp - EPOCH) << TIMESTAMP_SHIFT | workerId << WORKER_ID_SHIFT | sequence;
        return id;
    }
}
