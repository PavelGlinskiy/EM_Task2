package org.example;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        EvenOddExecutor example = new EvenOddExecutor();
        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.submit(example::printEven);
        executor.submit(example::printOdd);
        executor.shutdown();
    }
}