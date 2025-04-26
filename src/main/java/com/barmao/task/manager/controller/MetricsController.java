package com.barmao.task.manager.controller;

import com.barmao.task.manager.service.loadtest.TaskMetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private final TaskMetricsService metricsService;

    @Autowired
    public MetricsController(TaskMetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping
    public ResponseEntity<TaskMetricsService.TaskMetricsSnapshot> getMetrics() {
        return ResponseEntity.ok(metricsService.getMetricsSnapshot());
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetMetrics() {
        metricsService.resetMetrics();
        return ResponseEntity.ok("Metrics have been reset");
    }
}
