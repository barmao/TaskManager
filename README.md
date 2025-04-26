# Task Manager - Java Multithreading Demo

A Spring Boot application demonstrating Java multithreading and concurrency patterns through a task management system.

## Features

- Asynchronous task creation and processing
- Parallel task execution with controlled concurrency
- Thread-safe data access and state management
- Load testing capabilities for throughput analysis
- Real-time metrics for monitoring performance

## Getting Started

### Prerequisites

- Java 17 or higher
- Gradle 7.0 or higher

### Building the Application

```bash
./gradlew build
```

### Running the Application

```bash
./gradlew bootRun
```

The application will start on http://localhost:8080

## API Documentation

### Task Management Endpoints

#### Create a Task
- **URL**: `/api/tasks`
- **Method**: `POST`
- **Request Body**:
  ```json
  {
    "name": "Task Name",
    "description": "Task Description"
  }
  ```
- **Response**: The created task object with 201 Created status

#### Get All Tasks
- **URL**: `/api/tasks`
- **Method**: `GET`
- **Response**: List of all tasks

#### Get Task by ID
- **URL**: `/api/tasks/{id}`
- **Method**: `GET`
- **Response**: Single task object or 404 Not Found

#### Get Tasks by Status
- **URL**: `/api/tasks/status/{status}`
- **Method**: `GET`
- **Path Parameter**: `status` - One of: CREATED, PENDING, PROCESSING, COMPLETED, FAILED
- **Response**: List of tasks with the specified status

#### Process a Task
- **URL**: `/api/tasks/{id}/process`
- **Method**: `POST`
- **Response**: The processed task object

#### Process All Pending Tasks
- **URL**: `/api/tasks/process-pending`
- **Method**: `POST`
- **Response**: Accepted status with message

#### Cancel a Running Task
- **URL**: `/api/tasks/{id}/cancel`
- **Method**: `POST`
- **Response**: Success message or 404 Not Found if task isn't running

#### Get Task Statistics
- **URL**: `/api/tasks/statistics`
- **Method**: `GET`
- **Response**: Statistics about tasks in the system

### Load Testing Endpoints

#### Generate Test Tasks
- **URL**: `/api/load-test/generate`
- **Method**: `POST`
- **Request Body**:
  ```json
  {
    "totalTasks": 1000,
    "tasksPerMinute": 1000,
    "processImmediately": false
  }
  ```
- **Response**: Accepted status with message

#### Process Tasks in Parallel
- **URL**: `/api/load-test/process-parallel`
- **Method**: `POST`
- **Request Body**:
  ```json
  {
    "maxConcurrent": 50
  }
  ```
- **Response**: Accepted status with message

#### Run Full Load Test
- **URL**: `/api/load-test/full-load-test`
- **Method**: `POST`
- **Request Body**:
  ```json
  {
    "totalTasks": 1000,
    "tasksPerMinute": 1000,
    "processImmediately": true
  }
  ```
- **Response**: Load test results

### Metrics Endpoints

#### Get Current Metrics
- **URL**: `/api/metrics`
- **Method**: `GET`
- **Response**: Current metrics snapshot

#### Reset Metrics
- **URL**: `/api/metrics/reset`
- **Method**: `POST`
- **Response**: Success message

## Example Usage

### Basic Task Management

```bash
# Create a task
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Task","description":"Testing the API"}'

# Get all tasks
curl http://localhost:8080/api/tasks

# Process a task (replace {id} with actual task ID)
curl -X POST http://localhost:8080/api/tasks/{id}/process
```

### Load Testing

```bash
# Reset metrics before testing
curl -X POST http://localhost:8080/api/metrics/reset

# Generate 100 tasks at 60 tasks/minute (1 per second)
curl -X POST http://localhost:8080/api/load-test/generate \
  -H "Content-Type: application/json" \
  -d '{"totalTasks":100,"tasksPerMinute":60,"processImmediately":false}'

# Check metrics during generation
curl http://localhost:8080/api/metrics

# Process generated tasks with concurrency of 10
curl -X POST http://localhost:8080/api/load-test/process-parallel \
  -H "Content-Type: application/json" \
  -d '{"maxConcurrent":10}'

# Get final metrics
curl http://localhost:8080/api/metrics
```

### High-Volume Testing

```bash
# Run a full load test with 1000 tasks at 1000 tasks/minute
curl -X POST http://localhost:8080/api/load-test/full-load-test \
  -H "Content-Type: application/json" \
  -d '{"totalTasks":1000,"tasksPerMinute":1000,"processImmediately":true}'
```

## Java Concurrency Concepts Demonstrated

1. **Thread Pools**: Using Spring's `@Async` with custom thread pool executors
2. **CompletableFuture**: For non-blocking asynchronous operations
3. **Thread-Safe Collections**: ConcurrentHashMap for shared state
4. **Atomic Variables**: AtomicInteger for thread-safe counters
5. **Synchronization**: synchronized methods for thread safety
6. **Thread Coordination**: Using join and interruption
7. **Non-Blocking Concurrency**: Event-based communication
8. **Parallel Streaming**: Processing collections in parallel

## Configuration

Application configuration is in `src/main/resources/application.properties`. Key settings:

```properties
# Thread pool configuration
task.manager.executor.core-pool-size=4
task.manager.executor.max-pool-size=10
task.manager.executor.queue-capacity=100
task.manager.executor.keep-alive-seconds=60

# Server settings
server.port=8080
```

## Architecture

The application follows a standard Spring Boot architecture with:

- **Controller Layer**: REST endpoints for user interaction
- **Service Layer**: Business logic and task processing
- **Repository Layer**: Data access and storage
- **Model Layer**: Data entities and DTOs
- **Event System**: For decoupled component communication

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.