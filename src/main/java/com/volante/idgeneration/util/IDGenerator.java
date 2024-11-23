package com.volante.idgeneration.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IDGenerator {
    private static final Logger logger = LoggerFactory.getLogger(IDGenerator.class);

    // Custom epoch (January 1, 2024 Midnight UTC)
    private static final long EPOCH = 1704067200000L;

    // Bit lengths for different components
    public static final long WORKER_ID_BITS = 5L;
    private static final long THREAD_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 4L;

    // Maximum values for each component
    private static final long MAX_WORKER_ID = (1L << WORKER_ID_BITS) - 1L;
    private static final long MAX_THREAD_ID = (1L << THREAD_ID_BITS) - 1L;
    private static final long SEQUENCE_MASK = (1L << SEQUENCE_BITS) - 1L;

    // Bit shift amounts for each component
    private static final long THREAD_ID_SHIFT = SEQUENCE_BITS;
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS + THREAD_ID_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + THREAD_ID_BITS + WORKER_ID_BITS;

    // Instance variables
    private long workerId;
    private long lastTimestamp = -1L;
    private long sequence = 0L;

    public IDGenerator(long workerId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            logger.error("Invalid worker ID: {}", workerId);
            throw new IllegalArgumentException(
                    String.format("Worker ID must be between 0 and %d", MAX_WORKER_ID));
        }
        this.workerId = workerId;
        logger.info("Initialized IDGenerator with worker ID: {}", workerId);
    }

    public synchronized void setWorkerId(long workerId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            logger.error("Invalid worker ID in setter: {}", workerId);
            throw new IllegalArgumentException(
                    String.format("Worker ID must be between 0 and %d", MAX_WORKER_ID));
        }
        logger.info("Updating worker ID from {} to {}", this.workerId, workerId);
        this.workerId = workerId;
    }

    public synchronized long generateID() {
        long timestamp = System.currentTimeMillis();
        long threadId = Thread.currentThread().getId() & MAX_THREAD_ID;

        logger.info("Generating ID on Thread: {} (ID: {})",
                Thread.currentThread().getName(), threadId);

        if (timestamp < lastTimestamp) {
            logger.error("Clock moved backwards by {} milliseconds",
                    lastTimestamp - timestamp);
            throw new RuntimeException(
                    "Clock moved backwards. Refusing to generate ID for " +
                            (lastTimestamp - timestamp) + " milliseconds");
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                logger.debug("Sequence exhausted, waiting for next millisecond");
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }

        lastTimestamp = timestamp;

        long id = ((timestamp - EPOCH) << TIMESTAMP_SHIFT) |
                (workerId << WORKER_ID_SHIFT) |
                (threadId << THREAD_ID_SHIFT) |
                sequence;

        logger.debug("Generated ID: {} with components - ThreadId: {}, WorkerId: {}, Sequence: {}",
                id, threadId, workerId, sequence);

        return id;
    }

    private long waitNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        logger.debug("Waited until timestamp: {}", timestamp);
        return timestamp;
    }

    public static void decodeID(long id) {
        long timestamp = (id >> TIMESTAMP_SHIFT) + EPOCH;
        long workerId = (id >> WORKER_ID_SHIFT) & MAX_WORKER_ID;
        long threadId = (id >> THREAD_ID_SHIFT) & MAX_THREAD_ID;
        long sequence = id & SEQUENCE_MASK;

        logger.info("Decoded ID Components:");
        logger.info("Timestamp : {} ({})", timestamp, new java.util.Date(timestamp));
        logger.info("Worker ID : {}", workerId);
        logger.info("Thread ID : {}", threadId);
        logger.info("Sequence  : {}", sequence);
    }

    public static long getMaxWorkerId() {
        return MAX_WORKER_ID;
    }

    public static long getMaxThreadId() {
        return MAX_THREAD_ID;
    }

    public static long getMaxSequence() {
        return SEQUENCE_MASK;
    }
}