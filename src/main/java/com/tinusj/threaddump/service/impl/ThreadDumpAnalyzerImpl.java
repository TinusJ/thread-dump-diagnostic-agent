package com.tinusj.threaddump.service.impl;

import com.tinusj.threaddump.enums.Severity;
import com.tinusj.threaddump.enums.ThreadState;
import com.tinusj.threaddump.model.DiagnosticFinding;
import com.tinusj.threaddump.model.ThreadInfo;
import com.tinusj.threaddump.model.ThreadStatistics;
import com.tinusj.threaddump.parser.ThreadDumpParser;
import com.tinusj.threaddump.service.ThreadDumpAnalyzer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of ThreadDumpAnalyzer for analyzing thread dumps and generating diagnostic findings.
 */
@Service
@Slf4j
public class ThreadDumpAnalyzerImpl implements ThreadDumpAnalyzer {
    
    private final ThreadDumpParser parser;
    
    public ThreadDumpAnalyzerImpl(ThreadDumpParser parser) {
        this.parser = parser;
    }
    
    @Override
    public ThreadStatistics analyzeStatistics(String threadDumpContent) {
        List<ThreadInfo> threads = parser.parse(threadDumpContent);
        
        Map<ThreadState, Integer> threadsByState = new HashMap<>();
        int daemonThreads = 0;
        int blockedThreads = 0;
        int waitingThreads = 0;
        int runnableThreads = 0;
        
        for (ThreadInfo thread : threads) {
            ThreadState state = thread.state();
            threadsByState.merge(state, 1, Integer::sum);
            
            if (thread.daemon()) {
                daemonThreads++;
            }
            
            switch (state) {
                case BLOCKED -> blockedThreads++;
                case WAITING, TIMED_WAITING -> waitingThreads++;
                case RUNNABLE -> runnableThreads++;
            }
        }
        
        return new ThreadStatistics(
                threads.size(),
                threadsByState,
                daemonThreads,
                blockedThreads,
                waitingThreads,
                runnableThreads
        );
    }
    
    @Override
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
                .filter(thread -> thread.state() == ThreadState.BLOCKED)
                .filter(thread -> thread.lockName() != null)
                .collect(Collectors.toList());
        
        if (blockedThreads.size() >= 2) {
            findings.add(new DiagnosticFinding(
                    "POTENTIAL_DEADLOCK",
                    "Multiple threads are blocked waiting for locks",
                    Severity.HIGH,
                    blockedThreads.stream()
                            .map(ThreadInfo::name)
                            .collect(Collectors.toList()),
                    "Investigate lock ordering and consider using timeout-based locking",
                    null
            ));
        }
        
        return findings;
    }
    
    private List<DiagnosticFinding> checkThreadCount(List<ThreadInfo> threads) {
        List<DiagnosticFinding> findings = new ArrayList<>();
        
        if (threads.size() > 1000) {
            findings.add(new DiagnosticFinding(
                    "HIGH_THREAD_COUNT",
                    String.format("High number of threads detected: %d", threads.size()),
                    Severity.MEDIUM,
                    null,
                    "Consider using thread pools and reducing thread creation",
                    null
            ));
        }
        
        return findings;
    }
    
    private List<DiagnosticFinding> checkBlockedThreads(List<ThreadInfo> threads) {
        List<DiagnosticFinding> findings = new ArrayList<>();
        
        long blockedCount = threads.stream()
                .filter(thread -> thread.state() == ThreadState.BLOCKED)
                .count();
        
        if (blockedCount > 10) {
            findings.add(new DiagnosticFinding(
                    "HIGH_BLOCKED_THREADS",
                    String.format("High number of blocked threads: %d", blockedCount),
                    Severity.MEDIUM,
                    null,
                    "Review synchronization logic and reduce lock contention",
                    null
            ));
        }
        
        return findings;
    }
    
    private List<DiagnosticFinding> checkWaitingThreads(List<ThreadInfo> threads) {
        List<DiagnosticFinding> findings = new ArrayList<>();
        
        long waitingCount = threads.stream()
                .filter(thread -> thread.state() == ThreadState.WAITING ||
                               thread.state() == ThreadState.TIMED_WAITING)
                .count();
        
        if (waitingCount > 50) {
            findings.add(new DiagnosticFinding(
                    "HIGH_WAITING_THREADS",
                    String.format("High number of waiting threads: %d", waitingCount),
                    Severity.LOW,
                    null,
                    "Review thread coordination and consider reducing wait times",
                    null
            ));
        }
        
        return findings;
    }
    
    private List<DiagnosticFinding> detectHotspots(List<ThreadInfo> threads) {
        List<DiagnosticFinding> findings = new ArrayList<>();
        
        // Find common stack trace patterns (simplified hotspot detection)
        Map<String, Long> methodCounts = threads.stream()
                .flatMap(thread -> thread.stackTrace().stream())
                .filter(stackLine -> stackLine.contains("at "))
                .collect(Collectors.groupingBy(
                        stackLine -> extractMethodName(stackLine),
                        Collectors.counting()
                ));
        
        methodCounts.entrySet().stream()
                .filter(entry -> entry.getValue() > 5)
                .forEach(entry -> findings.add(new DiagnosticFinding(
                        "HOTSPOT",
                        String.format("Method appears frequently in stack traces: %s", entry.getKey()),
                        Severity.LOW,
                        null,
                        "Review method performance and consider optimization",
                        Map.of("method", entry.getKey(), "occurrences", entry.getValue())
                )));
        
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