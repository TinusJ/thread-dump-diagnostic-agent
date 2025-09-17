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

- **Java 17**
- **Spring Boot 3.3.0**
- **Spring AI MCP Server** (ready for integration when available)
- **Maven**
- **Lombok**
- **JUnit 5 + Mockito**

## Getting Started

### Prerequisites

- Java 17 or higher
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

The analyzer can detect:

1. **Deadlocks**: Identifies potential deadlock situations
2. **High Thread Count**: Warns when thread count exceeds thresholds
3. **Blocked Threads**: Detects excessive thread blocking
4. **Waiting Threads**: Identifies high numbers of waiting threads
5. **Performance Hotspots**: Finds frequently called methods in stack traces

## Sample Output

### JSON Report
```json
{
  "id": "uuid-here",
  "timestamp": "2023-12-01 10:30:00",
  "source": "sample.txt",
  "statistics": {
    "totalThreads": 25,
    "blockedThreads": 2,
    "waitingThreads": 10,
    "runnableThreads": 13
  },
  "findings": [
    {
      "type": "HIGH_WAITING_THREADS",
      "description": "High number of waiting threads: 10",
      "severity": "MEDIUM",
      "recommendation": "Review thread coordination and consider reducing wait times"
    }
  ],
  "suggestedFixes": [
    "Optimize thread coordination and reduce unnecessary waiting",
    "Review timeout values for blocking operations"
  ],
  "summary": "Analyzed 25 threads. 2 blocked, 10 waiting, 13 runnable. Found 1 issues (1 medium)."
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