# Thread Dump Diagnostic Agent

A Spring Boot 3 MCP-enabled diagnostic agent for analyzing Java thread dumps. This application provides both REST API endpoints and MCP (Model Context Protocol) server capabilities for analyzing thread dumps and generating comprehensive diagnostic reports.

## Features

- **Thread Dump Analysis**: Parse and analyze Java thread dumps to identify potential issues
- **Multiple Input Methods**: Accept thread dumps via REST API (text or file upload) and MCP server endpoints
- **Comprehensive Diagnostics**: Detect deadlocks, blocked threads, performance hotspots, and suspicious patterns
- **Configurable Output Formats**: Generate reports in JSON, XML, or plain text format
- **MCP Server Integration**: Expose analysis capabilities as MCP tools for AI assistants
- **Thread Statistics**: Detailed thread state analysis and statistics
- **Suggested Fixes**: Actionable recommendations based on analysis findings

## Technology Stack

- **Java 21**
- **Spring Boot 3.5.5**
- **Spring AI MCP Server** (ready for integration when available)
- **Maven**
- **Lombok**
- **JUnit 5 + Mockito**

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.6+

### Building the Application

```bash
mvn clean compile
```

### Running the Application

```bash
mvn spring-boot:run
```

The application will start on port 8080 with context path `/api`.

### Running Tests

```bash
mvn test
```

## API Usage

### REST Endpoints

#### Analyze Thread Dump from Text
```bash
POST /api/thread-dump/analyze-text?format=JSON
Content-Type: text/plain

[Thread dump content here]
```

#### Analyze Thread Dump from File Upload
```bash
POST /api/thread-dump/analyze-file?format=JSON
Content-Type: multipart/form-data

file: [thread dump file]
```

#### Get Supported Formats
```bash
POST /api/thread-dump/formats
```

### Supported Output Formats

- `JSON` (default) - application/json
- `XML` - application/xml  
- `TEXT` - text/plain

### Sample Thread Dump Content

You can test with a sample thread dump like this:

```
"main" #1 prio=5 os_prio=0 tid=0x00007f8c2c009000 nid=0x1234 runnable [0x00007f8c35a5e000]
   java.lang.Thread.State: RUNNABLE
   at java.lang.Object.wait(Native Method)
   at java.lang.Object.wait(Object.java:502)
   at java.util.concurrent.CountDownLatch.await(CountDownLatch.java:231)

"thread-pool-1" #10 prio=5 os_prio=0 tid=0x00007f8c2c123000 nid=0x5678 waiting on condition [0x00007f8c35b5f000]
   java.lang.Thread.State: WAITING (parking)
   at sun.misc.Unsafe.park(Native Method)
   at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
```

## MCP Server Usage

The application provides MCP-compatible functionality through REST endpoints. When Spring AI MCP Server becomes available, this can be easily integrated as a full MCP server.

### Current MCP Endpoints

#### POST /api/thread-dump/mcp-analyze
Provides MCP-compatible thread dump analysis.

**Request Body:**
```json
{
  "content": "[thread dump content]",
  "format": "JSON",
  "source": "mcp-input"
}
```

#### GET /api/thread-dump/mcp-tool-definition
Returns the MCP tool definition JSON for integration with MCP clients.

## Configuration

The application can be configured via `application.yml`:

```yaml
thread-dump:
  analysis:
    max-threads-warning: 1000
    deadlock-detection: true
    blocked-threads-threshold: 10
    waiting-threads-threshold: 50
  reports:
    default-format: JSON
    include-thread-details: true
    include-stack-traces: true
```

## Diagnostic Capabilities

The analyzer provides comprehensive thread dump analysis with the following enhanced capabilities:

### Core Analysis Features

1. **Advanced Deadlock Detection**: 
   - Identifies circular lock dependencies and potential deadlocks
   - Maps lock contention with specific thread and resource details
   - Provides severity escalation (CRITICAL) for deadlock situations

2. **Thread States Overview**: 
   - Detailed thread state statistics (RUNNABLE, WAITING, BLOCKED, TIMED_WAITING, etc.)
   - Thread group categorization and analysis
   - State distribution analysis with percentages

3. **Hotspots & Bottlenecks Analysis**:
   - **CPU Hotspots**: Identifies frequently executed methods in runnable threads
   - **Lock Contention Hotspots**: Detects methods causing excessive thread blocking
   - Performance impact scoring based on occurrence frequency

4. **Intelligent Thread Grouping**:
   - Automatic categorization into logical groups:
     - **GC**: Garbage collection threads
     - **HTTP/Web**: HTTP connectors, NIO, Tomcat, Jetty, Netty threads
     - **Database**: Connection pools, database-related threads
     - **Thread Pool**: Executor services, worker threads, schedulers
     - **JVM Internal**: JVM system threads, compiler threads
     - **Application**: Main application and business logic threads
     - **Other**: Uncategorized threads
   - Group-specific analysis and recommendations

5. **Suspicious Pattern Detection**:
   - **Thread Starvation**: Detects scenarios with many blocked threads and few runnable ones
   - **Excessive Blocking**: Identifies high percentages of blocked threads
   - **Identical Stack Traces**: Finds multiple threads with same execution patterns
   - **Resource Contention**: Analyzes shared resource bottlenecks

6. **Enhanced Recommendations**:
   - Specific, actionable suggestions for each finding type
   - Technology-specific recommendations (database, HTTP, threading)
   - Performance optimization guidance
   - Best practices for concurrent programming

### Detection Types

The analyzer can detect and classify these issue types:

- `POTENTIAL_DEADLOCK`: Circular lock dependencies (CRITICAL)
- `CPU_HOTSPOT`: High-frequency method execution (HIGH/MEDIUM)
- `LOCK_CONTENTION_HOTSPOT`: Methods causing blocking (HIGH)
- `THREAD_STARVATION`: Insufficient runnable threads (CRITICAL)
- `EXCESSIVE_BLOCKING`: High blocked thread percentage (HIGH)
- `EXCESSIVE_HTTP_THREADS`: Too many HTTP/Web threads (MEDIUM)
- `DATABASE_CONNECTION_CONTENTION`: DB connection pool issues (HIGH)
- `IDENTICAL_STACK_TRACES`: Resource contention patterns (MEDIUM)
- `HIGH_THREAD_COUNT`: Excessive total thread count (MEDIUM)
- `HIGH_BLOCKED_THREADS`: Many blocked threads (MEDIUM/HIGH)
- `HIGH_WAITING_THREADS`: Many waiting threads (LOW/MEDIUM)

## Sample Output

### Enhanced JSON Report
```json
{
  "id": "2a11a183-54f4-446d-8774-b38b4574ad2a",
  "timestamp": "2025-09-17T10:09:18",
  "source": "production-server",
  "statistics": {
    "totalThreads": 156,
    "threadsByState": {
      "RUNNABLE": 45,
      "BLOCKED": 23,
      "WAITING": 67,
      "TIMED_WAITING": 21
    },
    "daemonThreads": 89,
    "blockedThreads": 23,
    "waitingThreads": 88,
    "runnableThreads": 45,
    "threadGroups": {
      "HTTP/Web": 45,
      "Database": 28,
      "Thread Pool": 34,
      "GC": 12,
      "JVM Internal": 18,
      "Application": 19
    }
  },
  "findings": [
    {
      "type": "POTENTIAL_DEADLOCK",
      "description": "Multiple threads are blocked waiting for locks. 15 threads involved.",
      "severity": "CRITICAL",
      "affectedThreads": [
        "http-nio-8080-exec-1 (waiting for: 0x000000076ab62208)",
        "http-nio-8080-exec-2 (waiting for: 0x000000076ab62300)",
        "database-pool-3 (waiting for: 0x000000076ab62208)"
      ],
      "recommendation": "Implement consistent lock ordering across all threads and consider using timeout-based locking",
      "details": {
        "blockedThreadCount": 15,
        "lockContention": {
          "0x000000076ab62208": 8,
          "0x000000076ab62300": 7
        }
      }
    },
    {
      "type": "CPU_HOTSPOT",
      "description": "Method frequently appears in runnable thread stack traces: com.example.service.BusinessService.calculateResult (12 occurrences)",
      "severity": "HIGH",
      "affectedThreads": [
        "worker-thread-1",
        "worker-thread-3",
        "http-nio-8080-exec-5"
      ],
      "recommendation": "Profile and optimize this frequently executed method. Consider caching or algorithm improvements.",
      "details": {
        "method": "com.example.service.BusinessService.calculateResult",
        "occurrences": 12,
        "threadCount": 8
      }
    },
    {
      "type": "DATABASE_CONNECTION_CONTENTION",
      "description": "Multiple database threads are blocked: 12 out of 28",
      "severity": "HIGH",
      "affectedThreads": [
        "HikariPool-1-connection-1",
        "HikariPool-1-connection-2",
        "database-worker-3"
      ],
      "recommendation": "Check database connection pool configuration and query performance",
      "details": {
        "blockedThreads": 12,
        "totalDbThreads": 28
      }
    }
  ],
  "suggestedFixes": [
    "Implement consistent lock ordering across all threads",
    "Use timeout-based locking mechanisms (tryLock with timeout)",
    "Consider using higher-level concurrency utilities like java.util.concurrent",
    "Profile and optimize frequently called methods",
    "Consider caching results for expensive operations",
    "Increase database connection pool size",
    "Optimize database queries and reduce query execution time",
    "Review HTTP/Web thread group usage - 45 threads may be excessive"
  ],
  "summary": "Analyzed 156 threads. 23 blocked, 88 waiting, 45 runnable. Top groups: HTTP/Web(45), Thread Pool(34), Database(28). Found 3 issues (1 critical, 2 high, 0 medium)."
}
```

## Architecture

The application follows a layered architecture:

- **Controllers**: REST API endpoints (`ThreadDumpController`)
- **Services**: Business logic (`DiagnosticService`, `ThreadDumpAnalyzer`)
- **Models**: Data transfer objects (`DiagnosticReport`, `ThreadInfo`, etc.)
- **Parsers**: Thread dump parsing utilities (`ThreadDumpParser`)
- **Formatters**: Output formatting (`JsonReportFormatter`, `XmlReportFormatter`, `TextReportFormatter`)
- **Skills**: MCP server integration (`ThreadDumpAnalysisSkill`)

## Development

### Adding New Diagnostic Rules

1. Extend `ThreadDumpAnalyzer` with new analysis methods
2. Add new finding types to handle specific patterns
3. Update the suggested fixes logic in `DiagnosticService`

### Adding New Output Formats

1. Implement the `ReportFormatter` interface
2. Register the formatter as a Spring component
3. The `ReportFormatterService` will automatically pick it up

## Health Checks

The application exposes health check endpoints:

- `/api/actuator/health` - Application health status
- `/api/actuator/info` - Application information

## License

This project is part of the TinusJ thread dump diagnostic agent suite.