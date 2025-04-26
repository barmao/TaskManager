package com.barmao.task.manager.controller;

import com.barmao.task.manager.exception.ConcurrencyException;
import com.barmao.task.manager.model.Task;
import com.barmao.task.manager.service.ReportService;
import com.barmao.task.manager.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;
    private final ReportService reportService;


    @Autowired
    public TaskController(TaskService taskService, ReportService reportService) {
        this.taskService = taskService;
        this.reportService = reportService;
    }

    // SCENARIO 7: Non-blocking REST API with DeferredResult
    @PostMapping
    public DeferredResult<ResponseEntity<Task>> createTask(@RequestBody Map<String, String> taskRequest) {
        DeferredResult<ResponseEntity<Task>> deferredResult = new DeferredResult<>(30000L);

        String name = taskRequest.get("name");
        String description = taskRequest.get("description");

        // Process asynchronously
        taskService.createTaskAsync(name, description)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        deferredResult.setErrorResult(
                                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body("Error creating task: " + throwable.getMessage())
                        );
                    } else {
                        deferredResult.setResult(ResponseEntity.status(HttpStatus.CREATED).body(result));
                    }
                });

        return deferredResult;
    }

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(taskService.getTaskById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Task>> getTasksByStatus(
            @PathVariable Task.TaskStatus status) {
        return ResponseEntity.ok(taskService.getTasksByStatus(status));
    }

    // SCENARIO 8: Async task processing with CompletableFuture
    @PostMapping("/{id}/process")
    public DeferredResult<ResponseEntity<Task>> processTask(@PathVariable String id) {
        DeferredResult<ResponseEntity<Task>> deferredResult = new DeferredResult<>(60000L);

        taskService.processTaskAsync(id)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        Throwable cause = throwable.getCause();
                        if (cause instanceof ConcurrencyException) {
                            deferredResult.setErrorResult(
                                    ResponseEntity.status(HttpStatus.CONFLICT)
                                            .body("Task already being processed: " + cause.getMessage())
                            );
                        } else {
                            deferredResult.setErrorResult(
                                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                            .body("Error processing task: " + throwable.getMessage())
                            );
                        }
                    } else {
                        deferredResult.setResult(ResponseEntity.ok(result));
                    }
                });

        return deferredResult;
    }

    @PostMapping("/process-pending")
    public ResponseEntity<String> processPendingTasks() {
        taskService.processPendingTasksAsync();
        return ResponseEntity.accepted().body("Processing of pending tasks has been initiated");
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<String> cancelTask(@PathVariable String id) {
        boolean canceled = taskService.cancelTask(id);
        if (canceled) {
            return ResponseEntity.ok("Task has been canceled");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No running task found with ID: " + id);
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<TaskService.TaskStatistics> getTaskStatistics() {
        return ResponseEntity.ok(taskService.getTaskStatistics());
    }

    // SCENARIO 9: Asynchronous report generation
    @GetMapping("/report")
    public DeferredResult<ResponseEntity<String>> generateReport() {
        DeferredResult<ResponseEntity<String>> deferredResult = new DeferredResult<>(30000L);

        reportService.generateDetailedReportAsync()
                .whenComplete((report, throwable) -> {
                    if (throwable != null) {
                        deferredResult.setErrorResult(
                                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body("Error generating report: " + throwable.getMessage())
                        );
                    } else {
                        deferredResult.setResult(ResponseEntity.ok(report));
                    }
                });

        return deferredResult;
    }
}
