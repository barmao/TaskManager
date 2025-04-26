package com.barmao.task.manager.event;

import com.barmao.task.manager.service.loadtest.TaskMetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listener for task events that updates metrics.
 * This breaks the circular dependency by using events instead of direct method calls.
 */
@Component
public class TaskMetricsEventListener {

    private final TaskMetricsService metricsService;

    @Autowired
    public TaskMetricsEventListener(TaskMetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @EventListener
    public void handleTaskCreatedEvent(TaskEventPublisher.TaskCreatedEvent event) {
        metricsService.recordTaskCreated(event.getTask());
    }

    @EventListener
    public void handleTaskProcessingStartedEvent(TaskEventPublisher.TaskProcessingStartedEvent event) {
        metricsService.recordTaskProcessingStarted(event.getTask());
    }

    @EventListener
    public void handleTaskCompletedEvent(TaskEventPublisher.TaskCompletedEvent event) {
        metricsService.recordTaskCompleted(event.getTask(), event.getProcessingTimeMs());
    }
}
