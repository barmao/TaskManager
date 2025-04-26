package com.barmao.task.manager.service;

import com.barmao.task.manager.model.Task;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface TaskService {

    //Asynchronous task creation
    CompletableFuture<Task> createTaskAsync(String name, String description);

    //Get a task by ID
    Task getTaskById(String id);

    //Get all tasks
    List<Task> getAllTasks();

    //Get tasks by status
    List<Task> getTasksByStatus(Task.TaskStatus status);

    //Process a specific task
    CompletableFuture<Task> processTaskAsync(String id);

    //Process all pendin tasks in parallel
    CompletableFuture<List<Task>> processPendingTasksAsync();

    //Cancel a running task
    boolean cancelTask(String id);

    // Get task statistics
    TaskStatistics getTaskStatistics();

    // Data class for task statistics
    class TaskStatistics {
        private final long totalTasks;
        private final long pendingTasks;
        private final long processingTasks;
        private final long completedTasks;
        private final long failedTasks;

        public TaskStatistics(long totalTasks, long pendingTasks, long processingTasks,
                              long completedTasks, long failedTasks) {
            this.totalTasks = totalTasks;
            this.pendingTasks = pendingTasks;
            this.processingTasks = processingTasks;
            this.completedTasks = completedTasks;
            this.failedTasks = failedTasks;
        }

        public long getTotalTasks() {
            return totalTasks;
        }

        public long getPendingTasks() {
            return pendingTasks;
        }

        public long getProcessingTasks() {
            return processingTasks;
        }

        public long getCompletedTasks() {
            return completedTasks;
        }

        public long getFailedTasks() {
            return failedTasks;
        }
    }
}
