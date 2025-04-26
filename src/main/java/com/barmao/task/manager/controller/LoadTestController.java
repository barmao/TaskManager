package com.barmao.task.manager.controller;

import com.barmao.task.manager.service.loadtest.LoadTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Map;

@RestController
@RequestMapping("/api/load-test")
public class LoadTestController {

    private final LoadTestService loadTestService;

    @Autowired
    public LoadTestController(LoadTestService loadTestService) {
        this.loadTestService = loadTestService;
    }

    @PostMapping("/generate")
    public ResponseEntity<String> generateTasks(@RequestBody Map<String, Object> request) {
        int totalTasks = getIntParameter(request, "totalTasks", 1000);
        int tasksPerMinute = getIntParameter(request, "tasksPerMinute", 1000);
        boolean processImmediately = getBooleanParameter(request, "processImmediately", false);

        // Start the load test in the background
        loadTestService.generateLoad(totalTasks, tasksPerMinute, processImmediately);

        return ResponseEntity.accepted().body(
                String.format("Load test started: Generating %d tasks at %d tasks/minute (processImmediately=%s)",
                        totalTasks, tasksPerMinute, processImmediately)
        );
    }

    @PostMapping("/process-parallel")
    public ResponseEntity<String> processTasksInParallel(@RequestBody Map<String, Object> request) {
        int maxConcurrent = getIntParameter(request, "maxConcurrent", 10);

        // Start the parallel processing in the background
        loadTestService.processExistingTasksInParallel(maxConcurrent);

        return ResponseEntity.accepted().body(
                String.format("Started parallel processing of pending tasks with max concurrency of %d",
                        maxConcurrent)
        );
    }

    @PostMapping("/full-load-test")
    public DeferredResult<ResponseEntity<LoadTestService.LoadTestResult>> fullLoadTest(
            @RequestBody Map<String, Object> request) {

        DeferredResult<ResponseEntity<LoadTestService.LoadTestResult>> deferredResult =
                new DeferredResult<>(300000L); // 5-minute timeout

        int totalTasks = getIntParameter(request, "totalTasks", 1000);
        int tasksPerMinute = getIntParameter(request, "tasksPerMinute", 1000);
        boolean processImmediately = getBooleanParameter(request, "processImmediately", true);

        // Run complete load test and return results when finished
        loadTestService.generateLoad(totalTasks, tasksPerMinute, processImmediately)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        deferredResult.setErrorResult(
                                ResponseEntity.internalServerError().body("Load test failed: " + throwable.getMessage())
                        );
                    } else {
                        deferredResult.setResult(ResponseEntity.ok(result));
                    }
                });

        return deferredResult;
    }

    // Helper methods for parameter handling
    private int getIntParameter(Map<String, Object> request, String name, int defaultValue) {
        Object value = request.get(name);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private boolean getBooleanParameter(Map<String, Object> request, String name, boolean defaultValue) {
        Object value = request.get(name);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return defaultValue;
    }
}
