package com.barmao.task.manager.repository;

import com.barmao.task.manager.model.Task;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class TaskRepository {

    //Using ConcurrentHashMap for thread-safe operations
    private final Map<String, Task> tasks =  new ConcurrentHashMap<>();

    public Task save(Task task){
        tasks.put(task.getId(),task);
        return task;
    }

    public Optional<Task> findById(String id){
        return Optional.ofNullable(tasks.get(id));
    }

    public List<Task> findAll(){
        return new ArrayList<>(tasks.values());
    }

    public List<Task> findByStatus(Task.TaskStatus status) {
        List<Task> result = new ArrayList<>();
        for (Task task : tasks.values()){
            if(task.getStatus() == status) {
                result.add(task);
            }
        }

        return result;
    }

    public void deleteById(String id) {
        tasks.remove(id);
    }

    public boolean existsById(String id) {
        return tasks.containsKey(id);
    }

    public long count() {
        return tasks.size();
    }
}
