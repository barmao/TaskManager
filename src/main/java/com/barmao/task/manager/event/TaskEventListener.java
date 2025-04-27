package com.barmao.task.manager.event;

import com.barmao.task.manager.model.Task;
import com.barmao.task.manager.service.loadtest.TaskMetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * Combined listener for task events - handles both Spring application events
 * and JMS messages from ActiveMQ
 */
@Component
public class TaskEventListener {

    private static final Logger logger = LoggerFactory.getLogger(TaskEventListener.class);
    private final TaskMetricsService metricsService;

    @Autowired
    public TaskEventListener(TaskMetricsService metricsService) {
        this.metricsService = metricsService;
    }

    //
    // Spring Application Event Listeners
    //

    @EventListener
    public void handleTaskCreatedEvent(TaskEvents.TaskCreatedEvent event) {
        logger.debug("Received Spring application event: TaskCreatedEvent for task {}", event.getTaskId());

        Task task = createTaskFromEvent(event);
        metricsService.recordTaskCreated(task);
    }

    @EventListener
    public void handleTaskProcessingStartedEvent(TaskEvents.TaskProcessingStartedEvent event) {
        logger.debug("Received Spring application event: TaskProcessingStartedEvent for task {}", event.getTaskId());

        Task task = createTaskFromEvent(event);
        metricsService.recordTaskProcessingStarted(task);
    }

    @EventListener
    public void handleTaskCompletedEvent(TaskEvents.TaskCompletedEvent event) {
        logger.debug("Received Spring application event: TaskCompletedEvent for task {}", event.getTaskId());

        Task task = createTaskFromEvent(event);
        metricsService.recordTaskCompleted(task, event.getProcessingTimeMs());
    }

    //
    // JMS Listeners for ActiveMQ Messages
    //

    @JmsListener(destination = "task-created", containerFactory = "topicListenerFactory")
    public void receiveTaskCreatedMessage(TaskEvents.TaskCreatedEvent event) {
        logger.debug("Received JMS message: TaskCreatedEvent for task {}", event.getTaskId());
        // Process external message - useful for integrations with other systems
    }

    @JmsListener(destination = "task-processing", containerFactory = "topicListenerFactory")
    public void receiveTaskProcessingMessage(TaskEvents.TaskProcessingStartedEvent event) {
        logger.debug("Received JMS message: TaskProcessingStartedEvent for task {}", event.getTaskId());
        // Process external message
    }

    @JmsListener(destination = "task-completed", containerFactory = "topicListenerFactory")
    public void receiveTaskCompletedMessage(TaskEvents.TaskCompletedEvent event) {
        logger.debug("Received JMS message: TaskCompletedEvent for task {}", event.getTaskId());
        // Process external message
    }

    // Helper method to create a Task object from event data
    private Task createTaskFromEvent(TaskEvents.TaskEvent event) {
        Task task = new Task();
        task.setId(event.getTaskId());
        task.setName(event.getTaskName());
        task.setStatus(Task.TaskStatus.valueOf(event.getStatus()));
        return task;
    }
}
