package org.example;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        List<String> inputFiles = List.of("e4/test1.txt", "e4/test2.txt", "e4/test3.txt");
        int numWorkers = 3;
        int numReduceTasks = 3;

        System.out.println("=== MapReduce для подсчета слов ===");
        System.out.println("Входные файлы: " + inputFiles);
        System.out.println("Воркеров: " + numWorkers);
        System.out.println("Reduce задач: " + numReduceTasks);
        System.out.println();

        for (String fileName : inputFiles) {
            if (!new File(fileName).exists()) {
                System.err.println("ОШИБКА: Файл не найден: " + fileName);
                return;
            }
        }

        Coordinator coordinator = new Coordinator(inputFiles, numReduceTasks);

        ExecutorService executor = Executors.newFixedThreadPool(numWorkers);
        
        WordCountMapReduce wordCounter = new WordCountMapReduce();

        for (int i = 0; i < numWorkers; i++) {
            executor.submit(new Worker(i, coordinator, wordCounter));
        }

        executor.shutdown();
        try {
            if (executor.awaitTermination(30, TimeUnit.SECONDS)) {
                System.out.println("Все задачи выполнены успешно!");
                showResults(numReduceTasks);
            } else {
                System.err.println("Таймаут - воркеры не завершились вовремя");
            }
        } catch (InterruptedException e) {
            System.err.println("Прервано ожидание воркеров");
        }
    }

    private static void showResults(int numReduceTasks) {
        System.out.println("\n Результаты:");
        for (int i = 0; i < numReduceTasks; i++) {
            File outputFile = new File("e4", "mr-out-" + i);
            if (outputFile.exists()) {
                System.out.println("  " + outputFile.getName() + " - создан");
            }
        }
        System.out.println("\n Откройте файлы mr-out-* чтобы увидеть подсчет слов!");
    }
}