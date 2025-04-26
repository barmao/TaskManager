package com.barmao.task.manager.service;

import com.barmao.task.manager.event.TaskEventPublisher;
import com.barmao.task.manager.exception.ConcurrencyException;
import com.barmao.task.manager.model.Task;
import com.barmao.task.manager.repository.TaskRepository;
import com.barmao.task.manager.service.loadtest.TaskMetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TaskServiceImpl implements TaskService{

    private final TaskRepository taskRepository;
    private final TaskEventPublisher eventPublisher; // Use event publisher instead of direct service reference

    private final Random random = new Random();


    //Track running tasks for cancellation support
    private final Map<String, Thread> runningTaskThreads = new ConcurrentHashMap<>();

    @Autowired
    public TaskServiceImpl(TaskRepository taskRepository, TaskEventPublisher eventPublisher) {
        this.taskRepository = taskRepository;
        this.eventPublisher = eventPublisher;
    }


    // SCENARIO 1: Asynchronous task creation
    @Async("taskExecutor")
    @Override
    public CompletableFuture<Task> createTaskAsync(String name, String description) {

        //Simulate some processing time
        simulateProcessDelay(500,1500);

        Task newTask =  new Task(name,description);
        newTask.setStatus(Task.TaskStatus.PENDING);
        taskRepository.save(newTask);

        // Record metrics
        // Publish event instead of direct service call
        eventPublisher.publishTaskCreatedEvent(newTask);

        return CompletableFuture.completedFuture(newTask);
    }

    @Override
    public Task getTaskById(String id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found: " + id));
    }

    @Override
    public List<Task> getAllTasks() {
        return null;
    }

    @Override
    public List<Task> getTasksByStatus(Task.TaskStatus status) {
        return null;
    }

    // SCENARIO 2: Asynchronous task processing with thread tracking
    @Async("taskExecutor")
    @Override
    public CompletableFuture<Task> processTaskAsync(String id) {
        Task task = getTaskById(id);
        long startTime = System.currentTimeMillis();

        //Record current thread for potential cancellation
        Thread currentThread = Thread.currentThread();
        runningTaskThreads.put(id, currentThread);


        try {
            // Check if task is in a valid state for processing
            synchronized (task) {
                if (task.getStatus() != Task.TaskStatus.PENDING &&
                        task.getStatus() != Task.TaskStatus.CREATED) {
                    throw new ConcurrencyException("Task is already being processed or completed");
                }
                task.setStatus(Task.TaskStatus.PROCESSING);

                // Publish event instead of direct service call
                eventPublisher.publishTaskProcessingStartedEvent(task);
            }

            task.incrementAttempts();
            taskRepository.save(task);

            // Simulate task processing with progress updates
            processTaskWithProgress(task);

            // Update task status based on random success/failure
            boolean successful = random.nextDouble() > 0.2; // 80% success rate
            task.setStatus(successful ? Task.TaskStatus.COMPLETED : Task.TaskStatus.FAILED);

            return CompletableFuture.completedFuture(task);
        } catch (InterruptedException e) {
            // Handle thread interruption (for task cancellation)
            task.setStatus(Task.TaskStatus.FAILED);
            Thread.currentThread().interrupt();
            return CompletableFuture.completedFuture(task);
        } finally {
            // Calculate processing time and record metrics
            long processingTime = System.currentTimeMillis() - startTime;
            eventPublisher.publishTaskCompletedEvent(task, processingTime);

            // Remove thread reference when done
            runningTaskThreads.remove(id);
            taskRepository.save(task);
        }
    }

    // SCENARIO 3: Parallel task processing
    @Override
    public CompletableFuture<List<Task>> processPendingTasksAsync() {
        List<Task> pendingTasks = taskRepository.findByStatus(Task.TaskStatus.PENDING);
        List<CompletableFuture<Task>> futures = new ArrayList<>();

        // Start processing all pending tasks in parallel
        for (Task task : pendingTasks) {
            futures.add(processTaskAsync(task.getId()));
        }

        // Combine all futures into a single CompletableFuture
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    // Extract results from all futures
                    List<Task> processed = new ArrayList<>();
                    for (CompletableFuture<Task> future : futures) {
                        processed.add(future.join());
                    }
                    return processed;
                });
    }

    // SCENARIO 4: Task cancellation
    @Override
    public boolean cancelTask(String id) {
        Thread taskThread = runningTaskThreads.get(id);
        if (taskThread != null) {
            // Interrupt the thread executing the task
            taskThread.interrupt();

            // Wait briefly to allow the task to clean up
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Update task status
            Task task = getTaskById(id);
            task.setStatus(Task.TaskStatus.FAILED);
            taskRepository.save(task);

            return true;
        }
        return false;
    }

    @Override
    public TaskStatistics getTaskStatistics() {
        List<Task> allTasks = taskRepository.findAll();

        long totalTasks = allTasks.size();
        long pendingTasks = countTasksByStatus(allTasks, Task.TaskStatus.PENDING);
        long processingTasks = countTasksByStatus(allTasks, Task.TaskStatus.PROCESSING);
        long completedTasks = countTasksByStatus(allTasks, Task.TaskStatus.COMPLETED);
        long failedTasks = countTasksByStatus(allTasks, Task.TaskStatus.FAILED);

        return new TaskStatistics(totalTasks, pendingTasks, processingTasks, completedTasks, failedTasks);
    }


    // Helper methods
    private void simulateProcessDelay(int minMs, int maxMs) {
        try{
            int delay = random.nextInt(maxMs - minMs) + minMs;
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw  new RuntimeException("Processing was interrupted");
        }
    }

    private void processTaskWithProgress(Task task) throws InterruptedException {
        // Simulate a task that takes 5-10 seconds with progress updates
        int steps = 10;
        int baseDelay = random.nextInt(300) + 500; // 500-800ms per step

        for (int i = 1; i <= steps; i++) {
            // Check for interruption before each step
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Task processing was cancelled");
            }

            double progress = (i * 100.0) / steps;
            task.updateProgress(progress);
            taskRepository.save(task);

            Thread.sleep(baseDelay);
        }
    }

    private long countTasksByStatus(List<Task> tasks, Task.TaskStatus status) {
        return tasks.stream()
                .filter(task -> task.getStatus() == status)
                .count();
    }
}
