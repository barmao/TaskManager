package com.barmao.task.manager.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
public class Task {
    @Id
    private String id;

    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    @Transient // Not persisted - handled in memory
    private AtomicInteger processingAttempts = new AtomicInteger(0);

    private int attempts; // Persisted version of attempts
    private double progress;

    public enum TaskStatus {
        CREATED, PENDING, PROCESSING, COMPLETED, FAILED
    }

    public Task(String name, String description) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.status = TaskStatus.CREATED;
        this.createdAt = LocalDateTime.now();
        this.attempts = 0;
        this.progress = 0.0;
    }

    // Thread-safe method to update status
    public synchronized void setStatus(TaskStatus newStatus) {
        this.status = newStatus;
        if (newStatus == TaskStatus.COMPLETED) {
            this.completedAt = LocalDateTime.now();
            this.progress = 100.0;
        }
    }

    // Thread-safe method to increment attempts
    public int incrementAttempts() {
        int newValue = processingAttempts.incrementAndGet();
        this.attempts = newValue; // Update the persisted field
        return newValue;
    }

    // Thread-safe progress update
    public synchronized void updateProgress(double newProgress) {
        this.progress = Math.min(100.0, newProgress);
    }

    // Called before persisting to ensure attempts count is saved
    @PrePersist
    @PreUpdate
    public void prePersist() {
        if (processingAttempts != null) {
            this.attempts = processingAttempts.get();
        }
    }

    // Called after loading to initialize the AtomicInteger
    @PostLoad
    public void postLoad() {
        this.processingAttempts = new AtomicInteger(this.attempts);
    }
}
