package org.example;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class RingBuffer<T> {
    private final T[] buffer;
    private int head = 0;
    private int tail = 0;
    private int count = 0;
    private boolean closed = false;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private final Condition notFull = lock.newCondition();

    @SuppressWarnings("unchecked")
    public RingBuffer(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Buffer size must be greater than 0: " + size);
        }
        buffer = (T[]) new Object[size];
    }

    public void put(T item) throws InterruptedException {
        if (item == null) {
            throw new IllegalArgumentException("Null items are not allowed in the buffer");
        }
        lock.lock();
        try {
            while (count == buffer.length && !closed) {
                notFull.await();
            }
            if (closed) {
                throw new IllegalStateException("Buffer is closed");
            }
            buffer[tail] = item;
            tail = (tail + 1) % buffer.length;
            count++;
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public T get() throws InterruptedException {
        lock.lock();
        try {
            while (count == 0 && !closed) {
                notEmpty.await();
            }
            if (count == 0 && closed) {
                return null;
            }
            T item = buffer[head];
            head = (head + 1) % buffer.length;
            count--;
            notFull.signal();
            return item;
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }
    public boolean isEmpty() {
        lock.lock();
        try {
            return count == 0;
        } finally {
            lock.unlock();
        }
    }

    public void close() {
        lock.lock();
        try {
            closed = true;
            notEmpty.signalAll();
            notFull.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public boolean isFull() {
        lock.lock();
        try {
            return count == buffer.length;
        } finally {
            lock.unlock();
        }
    }
}

