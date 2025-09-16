package org.example;

import java.util.List;

public class Task {
    private final TaskType type;
    private final int taskId;
    private final String fileName;
    private final int reduceCount;
    private final List<String> intermediateFiles;

    public Task(TaskType type, int taskId, String fileName, int reduceCount, List<String> intermediateFiles) {
        this.type = type;
        this.taskId = taskId;
        this.fileName = fileName;
        this.reduceCount = reduceCount;
        this.intermediateFiles = intermediateFiles;
    }

    public TaskType getType() {
        return type;
    }

    public int getTaskId() {
        return taskId;
    }

    public String getFileName() {
        return fileName;
    }

    public int getReduceCount() {
        return reduceCount;
    }

    public List<String> getIntermediateFiles() {
        return intermediateFiles;
    }
}

