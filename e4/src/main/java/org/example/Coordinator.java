package org.example;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Coordinator {
    private final List<String> inputFiles;
    private final int reduceCount;
    
    private int nextMapTask = 0;
    private int nextReduceTask = 0;
    private int completedMapTasks = 0;
    private int completedReduceTasks = 0;
    
    private boolean mapPhaseFinished = false;
    private boolean allMapTasksCompleted = false;

    public Coordinator(List<String> inputFiles, int reduceCount) {
        if (inputFiles == null || inputFiles.isEmpty()) {
            throw new IllegalArgumentException("Список входных файлов не может быть null или пустым");
        }
        if (reduceCount <= 0) {
            throw new IllegalArgumentException("Количество Reduce задач должно быть положительным, а получено: " + reduceCount);
        }
        this.inputFiles = new ArrayList<>(inputFiles);
        this.reduceCount = reduceCount;
        
        System.out.println("Координатор создан:");
        System.out.println("Файлов для обработки: " + inputFiles.size());
        System.out.println("Reduce задач: " + reduceCount);
    }

    public synchronized Task requestTask() {
        if (!mapPhaseFinished) {
            if (nextMapTask < inputFiles.size()) {
                String fileName = inputFiles.get(nextMapTask);
                int taskId = nextMapTask++;
                System.out.println("Выдана MAP задача " + taskId + " для файла: " + fileName);
                return new Task(TaskType.MAP, taskId, fileName, reduceCount, null);
            } else {
                mapPhaseFinished = true;
                System.out.println("Все MAP задачи выданы, ждем завершения...");
                return new Task(TaskType.WAIT, -1, null, reduceCount, null);
            }
        }

        if (!allMapTasksCompleted) {
            if (completedMapTasks >= inputFiles.size()) {
                allMapTasksCompleted = true;
                System.out.println("Все MAP задачи завершены! Переходим к REDUCE фазе");
            } else {
                return new Task(TaskType.WAIT, -1, null, reduceCount, null);
            }
        }

        if (nextReduceTask < reduceCount) {
            int taskId = nextReduceTask++;
            
            List<String> intermediateFiles = new ArrayList<>();
            for (int i = 0; i < inputFiles.size(); i++) {
                File file = new File("e4", "mr-" + i + "-" + taskId);
                if (file.exists()) {
                    intermediateFiles.add(file.getPath());
                }
            }

            if (intermediateFiles.isEmpty()) {
                System.out.println("REDUCE задача " + taskId + " пропущена: нет промежуточных файлов");
                return new Task(TaskType.WAIT, -1, null, reduceCount, null);
            }
            
            System.out.println("Выдана REDUCE задача " + taskId + " с " + intermediateFiles.size() + " файлами");
            return new Task(TaskType.REDUCE, taskId, null, reduceCount, intermediateFiles);
        }

        System.out.println("Все задачи выполнены!");
        return new Task(TaskType.DONE, -1, null, reduceCount, null);
    }

    public synchronized void mapTaskCompleted(int taskId) {
        completedMapTasks++;
        System.out.println("MAP задача " + taskId + " завершена (" + completedMapTasks + "/" + inputFiles.size() + ")");
    }

    public synchronized void reduceTaskCompleted(int taskId) {
        completedReduceTasks++;
        System.out.println("REDUCE задача " + taskId + " завершена (" + completedReduceTasks + "/" + reduceCount + ")");
    }
}
