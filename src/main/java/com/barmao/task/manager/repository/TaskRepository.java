package com.barmao.task.manager.repository;

import com.barmao.task.manager.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {

    /**
     * Find tasks by status
     * @param status The task status to filter by
     * @return List of tasks with the specified status
     */
    List<Task> findByStatus(Task.TaskStatus status);
}
