package com.tinusj.threaddump;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Thread Dump Diagnostic Agent.
 * Spring Boot 3 MCP-enabled diagnostic agent for Java thread dumps.
 */
@SpringBootApplication
public class ThreadDumpDiagnosticAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThreadDumpDiagnosticAgentApplication.class, args);
    }
}