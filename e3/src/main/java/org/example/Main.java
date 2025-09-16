package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        RingBuffer<Integer> buffer = new RingBuffer<>(5);
        ExecutorService executor = Executors.newFixedThreadPool(4);

        executor.submit(runProducer(buffer, 1, 10, 100, "Producer1"));
        executor.submit(runProducer(buffer, 101, 110, 120, "Producer2"));

        executor.submit(runConsumer(buffer, 10, 150, "Consumer1"));
        executor.submit(runConsumer(buffer, 10, 180, "Consumer2"));

        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                logger.warn("Not all tasks finished with in the timeout, forcing shutdown");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        buffer.close();
        logger.info("All tasks finished.");
    }

    private static Runnable runProducer(RingBuffer<Integer> buffer, int start, int end, long delay, String name) {
        return () -> {
            try {
                for (int i = start; i <= end; i++) {
                    buffer.put(i);
                    logger.info("{} produced: {}", name, i);
                    Thread.sleep(delay);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("{} interrupted", name);
            }
        };
    }

    private static Runnable runConsumer(RingBuffer<Integer> buffer, int count, long delay, String name) {
        return () -> {
            try {
                for (int i = 0; i < count; i++) {
                    Integer item = buffer.get();
                    if (item == null) break;
                    logger.info("{} consumed: {}", name, item);
                    Thread.sleep(delay);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("{} interrupted", name);
            }
        };
    }
}