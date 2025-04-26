package com.barmao.task.manager.service.loadtest;

import com.barmao.task.manager.model.Task;
import com.barmao.task.manager.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for tracking real-time metrics about task processing
 */
@Service
public class TaskMetricsService {

    private static final Logger logger = LoggerFactory.getLogger(TaskMetricsService.class);

    // Removed TaskService dependency to break circular reference

    // Metrics counters
    private final AtomicInteger tasksCreated = new AtomicInteger(0);
    private final AtomicInteger tasksProcessed = new AtomicInteger(0);
    private final AtomicInteger tasksCompleted = new AtomicInteger(0);
    private final AtomicInteger tasksFailed = new AtomicInteger(0);
    private final AtomicLong totalProcessingTimeMs = new AtomicLong(0);

    // Rate tracking
    private final Map<Long, AtomicInteger> creationRateByMinute = new ConcurrentHashMap<>();
    private final Map<Long, AtomicInteger> processingRateByMinute = new ConcurrentHashMap<>();

    /**
     * Record a task creation event
     */
    public void recordTaskCreated(Task task) {
        tasksCreated.incrementAndGet();
        incrementRateCounter(creationRateByMinute);
    }

    /**
     * Record a task processing started
     */
    public void recordTaskProcessingStarted(Task task) {
        // No specific counter for this event
    }

    /**
     * Record a task completion event
     */
    public void recordTaskCompleted(Task task, long processingTimeMs) {
        tasksProcessed.incrementAndGet();
        incrementRateCounter(processingRateByMinute);

        if (task.getStatus() == Task.TaskStatus.COMPLETED) {
            tasksCompleted.incrementAndGet();
        } else if (task.getStatus() == Task.TaskStatus.FAILED) {
            tasksFailed.incrementAndGet();
        }

        totalProcessingTimeMs.addAndGet(processingTimeMs);
    }

    /**
     * Get the current processing rate (tasks/minute)
     */
    public int getCurrentProcessingRate() {
        return calculateCurrentRate(processingRateByMinute);
    }

    /**
     * Get the current creation rate (tasks/minute)
     */
    public int getCurrentCreationRate() {
        return calculateCurrentRate(creationRateByMinute);
    }

    /**
     * Get average processing time in milliseconds
     */
    public double getAverageProcessingTimeMs() {
        int processed = tasksProcessed.get();
        return processed > 0 ? (double) totalProcessingTimeMs.get() / processed : 0;
    }

    /**
     * Get total counts
     */
    public int getTotalTasksCreated() {
        return tasksCreated.get();
    }

    public int getTotalTasksProcessed() {
        return tasksProcessed.get();
    }

    public int getTotalTasksCompleted() {
        return tasksCompleted.get();
    }

    public int getTotalTasksFailed() {
        return tasksFailed.get();
    }

    /**
     * Reset all metrics
     */
    public void resetMetrics() {
        tasksCreated.set(0);
        tasksProcessed.set(0);
        tasksCompleted.set(0);
        tasksFailed.set(0);
        totalProcessingTimeMs.set(0);
        creationRateByMinute.clear();
        processingRateByMinute.clear();
    }

    // Helper method to increment rate counter for the current minute
    private void incrementRateCounter(Map<Long, AtomicInteger> rateMap) {
        long currentMinute = System.currentTimeMillis() / 60000; // Current minute timestamp
        rateMap.computeIfAbsent(currentMinute, k -> new AtomicInteger(0)).incrementAndGet();
    }

    // Calculate current rate by looking at recent entries
    private int calculateCurrentRate(Map<Long, AtomicInteger> rateMap) {
        long currentMinute = System.currentTimeMillis() / 60000;

        // Sum the counts from the current minute and the previous minute
        int sum = 0;

        // Current minute count
        AtomicInteger current = rateMap.get(currentMinute);
        if (current != null) {
            sum += current.get();
        }

        // Previous minute count
        AtomicInteger previous = rateMap.get(currentMinute - 1);
        if (previous != null) {
            sum += previous.get();
        }

        // Normalize to per-minute rate (simple average of last two minutes)
        return (sum + 1) / 2; // Add 1 to avoid division by zero
    }

    // Log metrics every minute
    @Scheduled(fixedRate = 60000)
    public void logMetrics() {
        logger.info("Task Metrics - Created: {}, Processed: {}, Completed: {}, Failed: {}, " +
                        "Avg Processing Time: {}ms, Current Creation Rate: {}/min, Current Processing Rate: {}/min",
                tasksCreated.get(),
                tasksProcessed.get(),
                tasksCompleted.get(),
                tasksFailed.get(),
                getAverageProcessingTimeMs(),
                getCurrentCreationRate(),
                getCurrentProcessingRate());

        // Clean up old entries from rate maps (keep only last 5 minutes)
        long currentMinute = System.currentTimeMillis() / 60000;
        cleanupOldEntries(creationRateByMinute, currentMinute);
        cleanupOldEntries(processingRateByMinute, currentMinute);
    }

    private void cleanupOldEntries(Map<Long, AtomicInteger> rateMap, long currentMinute) {
        rateMap.keySet().removeIf(minute -> minute < (currentMinute - 5));
    }

    // Get a snapshot of all metrics
    public TaskMetricsSnapshot getMetricsSnapshot() {
        return new TaskMetricsSnapshot(
                tasksCreated.get(),
                tasksProcessed.get(),
                tasksCompleted.get(),
                tasksFailed.get(),
                getAverageProcessingTimeMs(),
                getCurrentCreationRate(),
                getCurrentProcessingRate(),
                LocalDateTime.now()
        );
    }

    // Data class for metrics snapshot
    public static class TaskMetricsSnapshot {
        private final int totalCreated;
        private final int totalProcessed;
        private final int totalCompleted;
        private final int totalFailed;
        private final double avgProcessingTimeMs;
        private final int creationRate;
        private final int processingRate;
        private final LocalDateTime timestamp;

        public TaskMetricsSnapshot(
                int totalCreated,
                int totalProcessed,
                int totalCompleted,
                int totalFailed,
                double avgProcessingTimeMs,
                int creationRate,
                int processingRate,
                LocalDateTime timestamp) {
            this.totalCreated = totalCreated;
            this.totalProcessed = totalProcessed;
            this.totalCompleted = totalCompleted;
            this.totalFailed = totalFailed;
            this.avgProcessingTimeMs = avgProcessingTimeMs;
            this.creationRate = creationRate;
            this.processingRate = processingRate;
            this.timestamp = timestamp;
        }

        public int getTotalCreated() { return totalCreated; }
        public int getTotalProcessed() { return totalProcessed; }
        public int getTotalCompleted() { return totalCompleted; }
        public int getTotalFailed() { return totalFailed; }
        public double getAvgProcessingTimeMs() { return avgProcessingTimeMs; }
        public int getCreationRate() { return creationRate; }
        public int getProcessingRate() { return processingRate; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}
