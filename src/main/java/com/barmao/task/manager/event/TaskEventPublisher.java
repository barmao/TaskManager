package com.barmao.task.manager.event;

import com.barmao.task.manager.model.Task;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Publisher for task-related events. This breaks the circular dependency by
 * using the Observer pattern through Spring's event system.
 */
@Component
public class TaskEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public TaskEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void publishTaskCreatedEvent(Task task) {
        eventPublisher.publishEvent(new TaskCreatedEvent(task));
    }

    public void publishTaskProcessingStartedEvent(Task task) {
        eventPublisher.publishEvent(new TaskProcessingStartedEvent(task));
    }

    public void publishTaskCompletedEvent(Task task, long processingTimeMs) {
        eventPublisher.publishEvent(new TaskCompletedEvent(task, processingTimeMs));
    }

    // Event class definitions
    public static class TaskCreatedEvent {
        private final Task task;

        public TaskCreatedEvent(Task task) {
            this.task = task;
        }

        public Task getTask() {
            return task;
        }
    }

    public static class TaskProcessingStartedEvent {
        private final Task task;

        public TaskProcessingStartedEvent(Task task) {
            this.task = task;
        }

        public Task getTask() {
            return task;
        }
    }

    public static class TaskCompletedEvent {
        private final Task task;
        private final long processingTimeMs;

        public TaskCompletedEvent(Task task, long processingTimeMs) {
            this.task = task;
            this.processingTimeMs = processingTimeMs;
        }

        public Task getTask() {
            return task;
        }

        public long getProcessingTimeMs() {
            return processingTimeMs;
        }
    }
}
