package com.barmao.task.manager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

@Service
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);
    private final TaskService taskService;

    @Autowired
    public ReportService(TaskService taskService) {
        this.taskService = taskService;
    }

    // SCENARIO 5: Scheduled background task - runs every 30 seconds
    @Scheduled(fixedRate = 30000)
    public void generatePeriodicReport() {
        logger.info("Generating periodic task report at {}",
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));

        TaskService.TaskStatistics stats = taskService.getTaskStatistics();

        logger.info("Task Statistics:");
        logger.info("  Total tasks: {}", stats.getTotalTasks());
        logger.info("  Pending tasks: {}", stats.getPendingTasks());
        logger.info("  Processing tasks: {}", stats.getProcessingTasks());
        logger.info("  Completed tasks: {}", stats.getCompletedTasks());
        logger.info("  Failed tasks: {}", stats.getFailedTasks());
    }

    // SCENARIO 6: Asynchronous report generation with CompletableFuture chaining
    @Async("reportExecutor")
    public CompletableFuture<String> generateDetailedReportAsync() {
        logger.info("Starting detailed report generation on thread: {}",
                Thread.currentThread().getName());

        // First, get all the tasks
        return CompletableFuture.supplyAsync(() -> taskService.getAllTasks())
                .thenApply(tasks -> {
                    // Then process the tasks to generate report content
                    StringBuilder report = new StringBuilder();
                    report.append("Detailed Task Report - Generated at: ")
                            .append(LocalDateTime.now())
                            .append("\n\n");

                    report.append(String.format("%-36s | %-20s | %-10s | %-12s | %s\n",
                            "ID", "NAME", "STATUS", "PROGRESS", "ATTEMPTS"));
                    report.append("-".repeat(100)).append("\n");

                    tasks.forEach(task -> {
                        report.append(String.format("%-36s | %-20s | %-10s | %-12s | %s\n",
                                task.getId(),
                                task.getName(),
                                task.getStatus(),
                                task.getProgress() + "%",
                                task.getProcessingAttempts()));
                    });

                    // Simulate complex report generation
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Report generation interrupted", e);
                    }

                    return report.toString();
                });
    }
}