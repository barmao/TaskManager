package com.barmao.task.manager.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class Task {
    private final String id;
    private String name;
    private String description;
    private TaskStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private AtomicInteger processingAttempts;
    private double progress;

    public enum TaskStatus {
        CREATED, PENDING, PROCESSING, COMPLETED, FAILED
    }

    public  Task(String name, String description){
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.status = TaskStatus.CREATED;
        this.createdAt = LocalDateTime.now();
        this.processingAttempts = new AtomicInteger(0);
        this.progress = 0.0;
    }

    // Thread-safe method to update status
    public synchronized void setStatus(TaskStatus newStatus){
        this.status = newStatus;
        if(newStatus == TaskStatus.COMPLETED){
            this.completedAt = LocalDateTime.now();
            this.progress = 100.0;
        }
    }

    //Thread-safe method to increment attempts
    public int incrementAttempts(){
        return processingAttempts.incrementAndGet();
    }

    //Thread-safe progress update
    public synchronized void updateProgress(double newProgress){
        this.progress = Math.min(100.0, newProgress);
    }
}
