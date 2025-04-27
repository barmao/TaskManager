package com.barmao.task.manager.event;

import com.barmao.task.manager.model.Task;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Event classes for Task-related events that will be published to ActiveMQ
 */
public class TaskEvents {

    /**
     * Base class for all task events
     */
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static abstract class TaskEvent implements Serializable {
        private String taskId;
        private String taskName;
        private String status;
        private LocalDateTime timestamp = LocalDateTime.now();

        public TaskEvent(Task task) {
            this.taskId = task.getId();
            this.taskName = task.getName();
            this.status = task.getStatus().toString();
        }
    }

    /**
     * Event for when a task is created
     */
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TaskCreatedEvent extends TaskEvent {
        private String description;

        public TaskCreatedEvent(Task task) {
            super(task);
            this.description = task.getDescription();
        }
    }

    /**
     * Event for when task processing starts
     */
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TaskProcessingStartedEvent extends TaskEvent {
        private int attemptNumber;

        public TaskProcessingStartedEvent(Task task) {
            super(task);
            this.attemptNumber = task.getAttempts();
        }
    }

    /**
     * Event for when a task is completed (successfully or with failure)
     */
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TaskCompletedEvent extends TaskEvent {
        private boolean successful;
        private double progress;
        private long processingTimeMs;

        public TaskCompletedEvent(Task task, long processingTimeMs) {
            super(task);
            this.successful = task.getStatus() == Task.TaskStatus.COMPLETED;
            this.progress = task.getProgress();
            this.processingTimeMs = processingTimeMs;
        }
    }
}
