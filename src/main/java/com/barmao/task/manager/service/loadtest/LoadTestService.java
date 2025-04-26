package com.barmao.task.manager.service.loadtest;

import com.barmao.task.manager.model.Task;
import com.barmao.task.manager.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class LoadTestService {

    private static final Logger logger = LoggerFactory.getLogger(LoadTestService.class);
    private final TaskService taskService;
    private final TaskMetricsService metricsService;
    private final AtomicInteger taskCounter = new AtomicInteger(0);

    @Autowired
    public LoadTestService(TaskService taskService, TaskMetricsService metricsService) {
        this.taskService = taskService;
        this.metricsService = metricsService;
    }

    /**
     * Generates and processes a specified number of tasks at a controlled rate
     * @param totalTasks Total number of tasks to generate
     * @param tasksPerMinute Rate at which to generate tasks (tasks per minute)
     * @param processImmediately Whether to process tasks as they're created
     * @return CompletableFuture that completes when all tasks are generated
     */
    @Async("taskExecutor")
    public CompletableFuture<LoadTestResult> generateLoad(int totalTasks, int tasksPerMinute, boolean processImmediately) {
        logger.info("Starting load test: {} tasks at {} tasks/minute", totalTasks, tasksPerMinute);

        // Reset counter
        taskCounter.set(0);

        // Calculate timing
        long startTime = System.currentTimeMillis();
        double taskIntervalMs = 60_000.0 / tasksPerMinute; // Interval between tasks in ms
        int batchSize = Math.min(100, tasksPerMinute / 60); // Batch size, at least 1 per second but max 100
        if (batchSize < 1) batchSize = 1;

        double batchIntervalMs = batchSize * taskIntervalMs;

        logger.info("Task interval: {}ms, Batch size: {}, Batch interval: {}ms",
                taskIntervalMs, batchSize, batchIntervalMs);

        List<CompletableFuture<Task>> taskFutures = new ArrayList<>();

        try {
            for (int i = 0; i < totalTasks; i += batchSize) {
                long batchStart = System.currentTimeMillis();

                // Generate a batch of tasks
                for (int j = 0; j < batchSize && (i + j) < totalTasks; j++) {
                    int taskNum = taskCounter.incrementAndGet();
                    String taskName = "Load-Test-Task-" + taskNum;
                    String description = "Generated during load test at " + System.currentTimeMillis();

                    CompletableFuture<Task> future = taskService.createTaskAsync(taskName, description);

                    if (processImmediately) {
                        // Chain processing to creation
                        future = future.thenCompose(task ->
                                taskService.processTaskAsync(task.getId()));
                    }

                    taskFutures.add(future);

                    // Log progress periodically
                    if (taskNum % 100 == 0 || taskNum == totalTasks) {
                        logger.info("Generated {} tasks out of {}", taskNum, totalTasks);
                    }
                }

                // Calculate wait time to maintain the desired rate
                long batchDuration = System.currentTimeMillis() - batchStart;
                long sleepTime = Math.max(0, (long)batchIntervalMs - batchDuration);

                if (sleepTime > 0) {
                    Thread.sleep(sleepTime);
                }
            }

            long endTime = System.currentTimeMillis();
            double actualDurationSeconds = (endTime - startTime) / 1000.0;
            double actualRate = taskCounter.get() / actualDurationSeconds * 60;

            logger.info("Load test completed: Generated {} tasks in {} seconds ({} tasks/minute)",
                    taskCounter.get(), actualDurationSeconds, actualRate);

            return CompletableFuture.completedFuture(new LoadTestResult(
                    taskCounter.get(),
                    actualDurationSeconds,
                    actualRate,
                    processImmediately ? "Created and processed" : "Created only"
            ));

        } catch (InterruptedException e) {
            logger.warn("Load test was interrupted", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Load test interrupted", e);
        }
    }

    /**
     * Simulates high processing load by processing many existing tasks concurrently
     */
    public CompletableFuture<LoadTestResult> processExistingTasksInParallel(int maxConcurrent) {
        List<Task> pendingTasks = taskService.getTasksByStatus(Task.TaskStatus.PENDING);
        int taskCount = pendingTasks.size();

        if (taskCount == 0) {
            logger.info("No pending tasks found for parallel processing");
            return CompletableFuture.completedFuture(
                    new LoadTestResult(0, 0, 0, "No tasks to process"));
        }

        logger.info("Starting parallel processing of {} tasks with max concurrency of {}",
                taskCount, maxConcurrent);

        long startTime = System.currentTimeMillis();

        // Create a fixed thread pool for controlled concurrency
        ExecutorService executor = Executors.newFixedThreadPool(maxConcurrent);

        List<CompletableFuture<Task>> futures = new ArrayList<>();

        for (Task task : pendingTasks) {
            CompletableFuture<Task> future = taskService.processTaskAsync(task.getId());
            futures.add(future);
        }

        // Combine all futures
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]));

        return allFutures.thenApply(v -> {
            long endTime = System.currentTimeMillis();
            double durationSeconds = (endTime - startTime) / 1000.0;
            double rate = taskCount / durationSeconds * 60.0;

            logger.info("Parallel processing completed: Processed {} tasks in {} seconds ({} tasks/minute)",
                    taskCount, durationSeconds, rate);

            // Shut down the executor
            executor.shutdown();

            return new LoadTestResult(taskCount, durationSeconds, rate, "Parallel processing");
        });
    }

    // Result class to return load test statistics
    public static class LoadTestResult {
        private final int taskCount;
        private final double durationSeconds;
        private final double tasksPerMinute;
        private final String testType;

        public LoadTestResult(int taskCount, double durationSeconds, double tasksPerMinute, String testType) {
            this.taskCount = taskCount;
            this.durationSeconds = durationSeconds;
            this.tasksPerMinute = tasksPerMinute;
            this.testType = testType;
        }

        public int getTaskCount() { return taskCount; }
        public double getDurationSeconds() { return durationSeconds; }
        public double getTasksPerMinute() { return tasksPerMinute; }
        public String getTestType() { return testType; }
    }
}
