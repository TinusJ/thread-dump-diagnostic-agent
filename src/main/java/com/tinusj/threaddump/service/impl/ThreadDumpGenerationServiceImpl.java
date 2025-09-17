package com.tinusj.threaddump.service.impl;

import com.tinusj.threaddump.service.JavaProcessService;
import com.tinusj.threaddump.service.ThreadDumpGenerationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Implementation of ThreadDumpGenerationService using jstack tool.
 */
@Service
@Slf4j
public class ThreadDumpGenerationServiceImpl implements ThreadDumpGenerationService {

    private static final String JSTACK_COMMAND = "jstack";
    
    private final JavaProcessService javaProcessService;
    
    public ThreadDumpGenerationServiceImpl(JavaProcessService javaProcessService) {
        this.javaProcessService = javaProcessService;
    }

    @Override
    public String generateThreadDump(long pid) {
        log.info("Generating thread dump for PID: {}", pid);
        
        // First verify this is a valid Java process
        if (javaProcessService.getJavaProcessByPid(pid) == null) {
            throw new IllegalArgumentException("PID " + pid + " is not a valid Java process or not found");
        }
        
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(JSTACK_COMMAND, String.valueOf(pid));
            Process process = processBuilder.start();
            
            StringBuilder threadDump = new StringBuilder();
            
            // Read the thread dump output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    threadDump.append(line).append("\n");
                }
            }
            
            // Also read error stream for potential error messages
            StringBuilder errorOutput = new StringBuilder();
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                }
            }
            
            // Wait for process completion
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                String errorMsg = errorOutput.length() > 0 ? errorOutput.toString() : "Unknown error";
                log.error("jstack command failed with exit code: {}, error: {}", exitCode, errorMsg);
                throw new RuntimeException("Failed to generate thread dump for PID " + pid + ": " + errorMsg);
            }
            
            String result = threadDump.toString();
            if (result.trim().isEmpty()) {
                throw new RuntimeException("Thread dump generation produced no output for PID " + pid);
            }
            
            log.info("Successfully generated thread dump for PID: {} ({} characters)", pid, result.length());
            return result;
            
        } catch (IOException | InterruptedException e) {
            log.error("Error running jstack command for PID: {}", pid, e);
            throw new RuntimeException("Failed to generate thread dump for PID " + pid + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(JSTACK_COMMAND, "-h");
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            
            // jstack -h typically exits with code 0 or 1 but should not exit with command not found (127)
            boolean available = exitCode != 127;
            log.debug("Thread dump generation availability check: jstack available = {}", available);
            return available;
            
        } catch (IOException | InterruptedException e) {
            log.debug("Thread dump generation not available: {}", e.getMessage());
            return false;
        }
    }
}