package org.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Worker implements Runnable {
    private final Coordinator coordinator;
    private final WordCountMapReduce wordCounter;
    private final int workerId;

    public Worker(int workerId, Coordinator coordinator, WordCountMapReduce wordCounter) {
        this.workerId = workerId;
        this.coordinator = coordinator;
        this.wordCounter = wordCounter;
    }

    @Override
    public void run() {
        System.out.println("Воркер " + workerId + " запущен");
        
        while (true) {
            Task task = coordinator.requestTask();
            
            switch (task.getType()) {
                case MAP:
                    doMapTask(task);
                    break;
                case REDUCE:
                    doReduceTask(task);
                    break;
                case WAIT:
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        return;
                    }
                    continue;
                case DONE:
                    System.out.println("Воркер " + workerId + " завершил работу");
                    return;
            }
        }
    }

    private void doMapTask(Task task) {
        try {
            System.out.println("Воркер " + workerId + " обрабатывает файл: " + task.getFileName());
            
            String content = Files.readString(Paths.get(task.getFileName()));
            
            List<KeyValue> words = wordCounter.map(task.getFileName(), content);
            System.out.println("Найдено " + words.size() + " слов");
            
            writeToIntermediateFiles(task.getTaskId(), words, task.getReduceCount());
            
            coordinator.mapTaskCompleted(task.getTaskId());
            
        } catch (IOException e) {
            System.err.println("Ошибка при обработке файла " + task.getFileName() + ": " + e.getMessage());
        }
    }

    private void doReduceTask(Task task) {
        try {
            System.out.println("Воркер " + workerId + " выполняет REDUCE задачу " + task.getTaskId());
            
            List<KeyValue> allWords = new ArrayList<>();
            for (String fileName : task.getIntermediateFiles()) {
                allWords.addAll(readIntermediateFile(fileName));
            }
            System.out.println("Прочитано " + allWords.size() + " записей");

            Map<String, List<String>> groupedWords = new TreeMap<>();
            for (KeyValue kv : allWords) {
                groupedWords.computeIfAbsent(kv.getKey(), k -> new ArrayList<>()).add(kv.getValue());
            }

            File outputFile = new File("e4", "mr-out-" + task.getTaskId());
            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(outputFile.toPath(), StandardCharsets.UTF_8))) {
                for (Map.Entry<String, List<String>> entry : groupedWords.entrySet()) {
                    String word = entry.getKey();
                    List<String> counts = entry.getValue();

                    String totalCount = wordCounter.reduce(word, counts);
                    writer.println(word + " " + totalCount);
                }
            }
            System.out.println("Результат записан в " + outputFile);
            
            coordinator.reduceTaskCompleted(task.getTaskId());
            
        } catch (IOException e) {
            System.err.println("Ошибка при выполнении REDUCE задачи: " + e.getMessage());
        }
    }

    private void writeToIntermediateFiles(int mapTaskId, List<KeyValue> words, int reduceCount) throws IOException {
        Map<Integer, List<KeyValue>> groupedByReduce = new HashMap<>();
        
        for (KeyValue word : words) {
            int reduceTaskId = Math.abs(word.getKey().hashCode()) % reduceCount;
            groupedByReduce.computeIfAbsent(reduceTaskId, k -> new ArrayList<>()).add(word);
        }
        
        for (Map.Entry<Integer, List<KeyValue>> entry : groupedByReduce.entrySet()) {
            File fileName = new File("e4", "mr-" + mapTaskId + "-" + entry.getKey());
            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(fileName.toPath(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING))) {
                for (KeyValue word : entry.getValue()) {
                    writer.println(word.getKey() + " " + word.getValue());
                }
            }
        }
    }

    private List<KeyValue> readIntermediateFile(String fileName) throws IOException {
        List<KeyValue> words = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(fileName), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split(" ", 2);
                if (parts.length == 2) {
                    words.add(new KeyValue(parts[0], parts[1]));
                }
            }
        }
        return words;
    }
}
