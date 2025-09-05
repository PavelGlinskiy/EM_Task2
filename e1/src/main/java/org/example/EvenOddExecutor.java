package org.example;

import java.util.concurrent.atomic.AtomicInteger;

public class EvenOddExecutor {
    private static final int LIMIT = 10;
    private final AtomicInteger number = new AtomicInteger(0);

    public void printEven() {
        while (true) {
            int current = number.get();
            if (current >= LIMIT) break;
            if (current % 2 == 0) {
                System.out.println("Even: " + current);
                number.incrementAndGet();
            }
        }
    }

    public void printOdd() {
        while (true) {
            int current = number.get();
            if (current >= LIMIT) break;
            if (current % 2 != 0) {
                System.out.println("Odd: " + current);
                number.incrementAndGet();
            }
        }
    }
}
