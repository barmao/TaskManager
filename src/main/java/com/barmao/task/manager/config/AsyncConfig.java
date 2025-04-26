package com.barmao.task.manager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Value("${task.manager.executor.core-pool-size:4}")
    private int corePoolSize;

    @Value("${task.manager.executor.max-pool-size:10}")
    private int maxPoolSize;

    @Value("${task.manager.executor.queue-capacity:100}")
    private int queueCapacity;

    @Value("${task.manager.executor.keep-alive-seconds:60}")
    private int keepAliveSeconds;

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize); // Number of core threads
        executor.setMaxPoolSize(maxPoolSize); // Max threads when queue is full
        executor.setQueueCapacity(queueCapacity); // Queue capacity before scaling up
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setThreadNamePrefix("TaskThread-"); // Thread name prefix for debugging

        // Rejection policy: Caller runs - good for controlled overload scenarios
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }

    @Bean(name = "reportExecutor")
    public Executor reportExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("ReportThread-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "highLoadExecutor")
    public Executor highLoadExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // Higher capacity for load testing
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(1000);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("HighLoad-");

        // Use CallerRunsPolicy for backpressure instead of throwing exceptions
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }
}
