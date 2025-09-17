package com.tinusj.threaddump.service;

import com.tinusj.threaddump.model.DiagnosticFinding;
import com.tinusj.threaddump.model.ThreadInfo;
import com.tinusj.threaddump.model.ThreadState;
import com.tinusj.threaddump.model.ThreadStatistics;
import com.tinusj.threaddump.parser.ThreadDumpParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for analyzing thread dumps and generating diagnostic findings.
 */
@Service
@Slf4j
public class ThreadDumpAnalyzer {
    
    private final ThreadDumpParser parser;
    
    public ThreadDumpAnalyzer(ThreadDumpParser parser) {
        this.parser = parser;
    }
    
    /**
     * Analyzes thread dump content and generates statistics.
     * 
     * @param threadDumpContent the raw thread dump content
     * @return thread statistics
     */
    public ThreadStatistics analyzeStatistics(String threadDumpContent) {
        List<ThreadInfo> threads = parser.parse(threadDumpContent);
        
        Map<ThreadState, Integer> threadsByState = new HashMap<>();
        int daemonThreads = 0;
        int blockedThreads = 0;
        int waitingThreads = 0;
        int runnableThreads = 0;
        
        for (ThreadInfo thread : threads) {
            ThreadState state = thread.getState();
            threadsByState.merge(state, 1, Integer::sum);
            
            if (thread.isDaemon()) {
                daemonThreads++;
            }
            
            switch (state) {
                case BLOCKED -> blockedThreads++;
                case WAITING, TIMED_WAITING -> waitingThreads++;
                case RUNNABLE -> runnableThreads++;
            }
        }
        
        return ThreadStatistics.builder()
                .totalThreads(threads.size())
                .threadsByState(threadsByState)
                .daemonThreads(daemonThreads)
                .blockedThreads(blockedThreads)
                .waitingThreads(waitingThreads)
                .runnableThreads(runnableThreads)
                .build();
    }
    
    /**
     * Analyzes thread dump content and generates diagnostic findings.
     * 
     * @param threadDumpContent the raw thread dump content
     * @return list of diagnostic findings
     */
    public List<DiagnosticFinding> analyzeFindings(String threadDumpContent) {
        List<DiagnosticFinding> findings = new ArrayList<>();
        List<ThreadInfo> threads = parser.parse(threadDumpContent);
        
        log.debug("Analyzing {} threads for diagnostic findings", threads.size());
        
        // Check for deadlocks
        findings.addAll(detectDeadlocks(threads));
        
        // Check for high thread count
        findings.addAll(checkThreadCount(threads));
        
        // Check for blocked threads
        findings.addAll(checkBlockedThreads(threads));
        
        // Check for waiting threads
        findings.addAll(checkWaitingThreads(threads));
        
        // Check for hotspots
        findings.addAll(detectHotspots(threads));
        
        return findings;
    }
    
    private List<DiagnosticFinding> detectDeadlocks(List<ThreadInfo> threads) {
        List<DiagnosticFinding> findings = new ArrayList<>();
        
        // Simple deadlock detection based on blocked threads with lock information
        List<ThreadInfo> blockedThreads = threads.stream()
                .filter(thread -> thread.getState() == ThreadState.BLOCKED)
                .filter(thread -> thread.getLockName() != null)
                .collect(Collectors.toList());
        
        if (blockedThreads.size() >= 2) {
            findings.add(DiagnosticFinding.builder()
                    .type("POTENTIAL_DEADLOCK")
                    .description("Multiple threads are blocked waiting for locks")
                    .severity(DiagnosticFinding.Severity.HIGH)
                    .affectedThreads(blockedThreads.stream()
                            .map(ThreadInfo::getName)
                            .collect(Collectors.toList()))
                    .recommendation("Investigate lock ordering and consider using timeout-based locking")
                    .build());
        }
        
        return findings;
    }
    
    private List<DiagnosticFinding> checkThreadCount(List<ThreadInfo> threads) {
        List<DiagnosticFinding> findings = new ArrayList<>();
        
        if (threads.size() > 1000) {
            findings.add(DiagnosticFinding.builder()
                    .type("HIGH_THREAD_COUNT")
                    .description(String.format("High number of threads detected: %d", threads.size()))
                    .severity(DiagnosticFinding.Severity.MEDIUM)
                    .recommendation("Consider using thread pools and reducing thread creation")
                    .build());
        }
        
        return findings;
    }
    
    private List<DiagnosticFinding> checkBlockedThreads(List<ThreadInfo> threads) {
        List<DiagnosticFinding> findings = new ArrayList<>();
        
        long blockedCount = threads.stream()
                .filter(thread -> thread.getState() == ThreadState.BLOCKED)
                .count();
        
        if (blockedCount > 10) {
            findings.add(DiagnosticFinding.builder()
                    .type("HIGH_BLOCKED_THREADS")
                    .description(String.format("High number of blocked threads: %d", blockedCount))
                    .severity(DiagnosticFinding.Severity.MEDIUM)
                    .recommendation("Review synchronization logic and reduce lock contention")
                    .build());
        }
        
        return findings;
    }
    
    private List<DiagnosticFinding> checkWaitingThreads(List<ThreadInfo> threads) {
        List<DiagnosticFinding> findings = new ArrayList<>();
        
        long waitingCount = threads.stream()
                .filter(thread -> thread.getState() == ThreadState.WAITING ||
                               thread.getState() == ThreadState.TIMED_WAITING)
                .count();
        
        if (waitingCount > 50) {
            findings.add(DiagnosticFinding.builder()
                    .type("HIGH_WAITING_THREADS")
                    .description(String.format("High number of waiting threads: %d", waitingCount))
                    .severity(DiagnosticFinding.Severity.LOW)
                    .recommendation("Review thread coordination and consider reducing wait times")
                    .build());
        }
        
        return findings;
    }
    
    private List<DiagnosticFinding> detectHotspots(List<ThreadInfo> threads) {
        List<DiagnosticFinding> findings = new ArrayList<>();
        
        // Find common stack trace patterns (simplified hotspot detection)
        Map<String, Long> methodCounts = threads.stream()
                .flatMap(thread -> thread.getStackTrace().stream())
                .filter(stackLine -> stackLine.contains("at "))
                .collect(Collectors.groupingBy(
                        stackLine -> extractMethodName(stackLine),
                        Collectors.counting()
                ));
        
        methodCounts.entrySet().stream()
                .filter(entry -> entry.getValue() > 5)
                .forEach(entry -> findings.add(DiagnosticFinding.builder()
                        .type("HOTSPOT")
                        .description(String.format("Method appears frequently in stack traces: %s", entry.getKey()))
                        .severity(DiagnosticFinding.Severity.LOW)
                        .recommendation("Review method performance and consider optimization")
                        .details(Map.of("method", entry.getKey(), "occurrences", entry.getValue()))
                        .build()));
        
        return findings;
    }
    
    private String extractMethodName(String stackLine) {
        // Simple method name extraction from stack trace line
        if (stackLine.contains("(")) {
            return stackLine.substring(stackLine.indexOf("at ") + 3, stackLine.indexOf("("));
        }
        return stackLine;
    }
}