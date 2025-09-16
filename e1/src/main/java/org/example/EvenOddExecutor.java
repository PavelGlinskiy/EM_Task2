package org.example;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class EvenOddExecutor {
    private static final int LIMIT = 10;
    private final AtomicInteger number = new AtomicInteger(0);

    private final Lock lock = new ReentrantLock();
    private final Condition evenTurn = lock.newCondition();
    private final Condition oddTurn = lock.newCondition();

    public void printEven() {
        while (true) {
            lock.lock();
            try {
                while (number.get() < LIMIT && number.get() % 2 != 0) {
                    evenTurn.await();
                }
                if (number.get() >= LIMIT) break;

                System.out.println("Even: " + number.getAndIncrement());

                oddTurn.signal();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        }
    }

    public void printOdd() {
        while (true) {
            lock.lock();
            try {
                while (number.get() < LIMIT && number.get() % 2 == 0) {
                    oddTurn.await();
                }
                if (number.get() >= LIMIT) break;

                System.out.println("Odd: " + number.getAndIncrement());

                evenTurn.signal();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        }
    }
}
