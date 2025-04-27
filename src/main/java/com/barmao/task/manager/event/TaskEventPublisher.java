package com.barmao.task.manager.event;

import com.barmao.task.manager.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * Publisher for task-related events using both Spring's ApplicationEventPublisher
 * and ActiveMQ for broader integration capabilities.
 */
@Component
public class TaskEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(TaskEventPublisher.class);
    private static final String TASK_EVENTS_TOPIC = "task-events";
    private static final String TASK_CREATED_TOPIC = "task-created";
    private static final String TASK_PROCESSING_TOPIC = "task-processing";
    private static final String TASK_COMPLETED_TOPIC = "task-completed";

    private final ApplicationEventPublisher eventPublisher;
    private final JmsTemplate jmsTemplate;

    @Autowired
    public TaskEventPublisher(ApplicationEventPublisher eventPublisher, JmsTemplate jmsTemplate) {
        this.eventPublisher = eventPublisher;
        this.jmsTemplate = jmsTemplate;
    }

    public void publishTaskCreatedEvent(Task task) {
        // Create the event
        TaskEvents.TaskCreatedEvent event = new TaskEvents.TaskCreatedEvent(task);

        // Publish to Spring's event system (for internal app use)
        eventPublisher.publishEvent(event);

        // Publish to ActiveMQ topics (for external integrations)
        try {
            jmsTemplate.convertAndSend(TASK_CREATED_TOPIC, event);
            logger.debug("Published TaskCreatedEvent to JMS for task: {}", task.getId());
        } catch (JmsException e) {
            // Log but don't fail if JMS publishing fails
            logger.error("Failed to publish TaskCreatedEvent to JMS: {}", e.getMessage());
        }
    }

    public void publishTaskProcessingStartedEvent(Task task) {
        // Create the event
        TaskEvents.TaskProcessingStartedEvent event = new TaskEvents.TaskProcessingStartedEvent(task);

        // Publish to Spring's event system
        eventPublisher.publishEvent(event);

        // Publish to ActiveMQ topics
        try {
            jmsTemplate.convertAndSend(TASK_PROCESSING_TOPIC, event);
            logger.debug("Published TaskProcessingStartedEvent to JMS for task: {}", task.getId());
        } catch (JmsException e) {
            logger.error("Failed to publish TaskProcessingStartedEvent to JMS: {}", e.getMessage());
        }
    }

    public void publishTaskCompletedEvent(Task task, long processingTimeMs) {
        // Create the event
        TaskEvents.TaskCompletedEvent event = new TaskEvents.TaskCompletedEvent(task, processingTimeMs);

        // Publish to Spring's event system
        eventPublisher.publishEvent(event);

        // Publish to ActiveMQ topics
        try {
            jmsTemplate.convertAndSend(TASK_COMPLETED_TOPIC, event);
            logger.debug("Published TaskCompletedEvent to JMS for task: {}", task.getId());
        } catch (JmsException e) {
            logger.error("Failed to publish TaskCompletedEvent to JMS: {}", e.getMessage());
        }
    }
}