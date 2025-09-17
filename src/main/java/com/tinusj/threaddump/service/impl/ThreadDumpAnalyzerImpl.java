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
        Map<String, Integer> threadGroups = new HashMap<>();
        int daemonThreads = 0;
        int blockedThreads = 0;
        int waitingThreads = 0;
        int runnableThreads = 0;
        
        for (ThreadInfo thread : threads) {
            ThreadState state = thread.state();
            threadsByState.merge(state, 1, Integer::sum);
            
            // Categorize threads by type
            String threadGroup = categorizeThread(thread);
            threadGroups.merge(threadGroup, 1, Integer::sum);
            
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
                runnableThreads,
                threadGroups
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
        
        // Check for thread grouping issues
        findings.addAll(analyzeThreadGroups(threads));
        
        // Check for suspicious patterns
        findings.addAll(detectSuspiciousPatterns(threads));
        
        return findings;
    }
    
    private List<DiagnosticFinding> detectDeadlocks(List<ThreadInfo> threads) {
        List<DiagnosticFinding> findings = new ArrayList<>();
        
        // Enhanced deadlock detection based on blocked threads with lock information
        List<ThreadInfo> blockedThreads = threads.stream()
                .filter(thread -> thread.state() == ThreadState.BLOCKED)
                .filter(thread -> thread.lockName() != null)
                .collect(Collectors.toList());
        
        if (blockedThreads.size() >= 2) {
            // Group blocked threads by lock they're waiting for
            Map<String, List<ThreadInfo>> threadsByLock = blockedThreads.stream()
                    .collect(Collectors.groupingBy(ThreadInfo::lockName));
            
            // Look for circular dependencies
            Map<String, String> lockOwnership = new HashMap<>();
            for (ThreadInfo thread : blockedThreads) {
                if (thread.lockOwner() != null) {
                    lockOwnership.put(thread.lockOwner(), thread.lockName());
                }
            }
            
            // Check for potential circular wait
            boolean potentialDeadlock = false;
            List<String> involvedThreads = new ArrayList<>();
            
            for (Map.Entry<String, List<ThreadInfo>> entry : threadsByLock.entrySet()) {
                String lockName = entry.getKey();
                List<ThreadInfo> waitingThreads = entry.getValue();
                
                if (waitingThreads.size() > 1) {
                    potentialDeadlock = true;
                    involvedThreads.addAll(waitingThreads.stream()
                            .map(ThreadInfo::name)
                            .collect(Collectors.toList()));
                }
            }
            
            if (potentialDeadlock) {
                findings.add(new DiagnosticFinding(
                        "POTENTIAL_DEADLOCK",
                        String.format("Multiple threads are blocked waiting for locks. %d threads involved.", 
                                blockedThreads.size()),
                        Severity.CRITICAL,
                        involvedThreads,
                        "Implement consistent lock ordering across all threads and consider using timeout-based locking",
                        Map.of(
                                "blockedThreadCount", blockedThreads.size(),
                                "lockContention", threadsByLock.entrySet().stream()
                                        .collect(Collectors.toMap(
                                                Map.Entry::getKey,
                                                e -> e.getValue().size()
                                        ))
                        )
                ));
            }
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
        
        List<ThreadInfo> blockedThreads = threads.stream()
                .filter(thread -> thread.state() == ThreadState.BLOCKED)
                .collect(Collectors.toList());
        
        long blockedCount = blockedThreads.size();
        
        if (blockedCount > 10) {
            // Analyze what the blocked threads are waiting for
            Map<String, List<ThreadInfo>> blockedByLock = blockedThreads.stream()
                    .filter(t -> t.lockName() != null)
                    .collect(Collectors.groupingBy(ThreadInfo::lockName));
            
            List<String> topBlockedThreads = blockedThreads.stream()
                    .limit(10)
                    .map(t -> String.format("%s (waiting for: %s)", 
                            t.name(), t.lockName() != null ? t.lockName() : "unknown"))
                    .collect(Collectors.toList());
            
            findings.add(new DiagnosticFinding(
                    "HIGH_BLOCKED_THREADS",
                    String.format("High number of blocked threads: %d. Top contended locks: %s", 
                            blockedCount, 
                            blockedByLock.entrySet().stream()
                                    .sorted(Map.Entry.<String, List<ThreadInfo>>comparingByValue(
                                            (a, b) -> Integer.compare(b.size(), a.size())))
                                    .limit(3)
                                    .map(e -> String.format("%s(%d threads)", e.getKey(), e.getValue().size()))
                                    .collect(Collectors.joining(", "))),
                    blockedCount > 50 ? Severity.HIGH : Severity.MEDIUM,
                    topBlockedThreads,
                    "Review synchronization logic and reduce lock contention. Consider lock-free alternatives or finer-grained locking.",
                    Map.of(
                            "blockedCount", blockedCount,
                            "lockContention", blockedByLock.entrySet().stream()
                                    .collect(Collectors.toMap(
                                            Map.Entry::getKey,
                                            e -> e.getValue().size()
                                    ))
                    )
            ));
        }
        
        return findings;
    }
    
    private List<DiagnosticFinding> checkWaitingThreads(List<ThreadInfo> threads) {
        List<DiagnosticFinding> findings = new ArrayList<>();
        
        List<ThreadInfo> waitingThreads = threads.stream()
                .filter(thread -> thread.state() == ThreadState.WAITING ||
                               thread.state() == ThreadState.TIMED_WAITING)
                .collect(Collectors.toList());
        
        long waitingCount = waitingThreads.size();
        
        if (waitingCount > 50) {
            // Analyze what threads are waiting for
            Map<String, List<ThreadInfo>> waitingPatterns = waitingThreads.stream()
                    .collect(Collectors.groupingBy(thread -> {
                        if (!thread.stackTrace().isEmpty()) {
                            String topStack = thread.stackTrace().get(0);
                            if (topStack.contains("Object.wait") || topStack.contains("Thread.sleep")) {
                                return topStack.substring(topStack.indexOf("at ") + 3);
                            }
                        }
                        return "Unknown wait";
                    }));
            
            List<String> topWaitingThreads = waitingThreads.stream()
                    .limit(10)
                    .map(t -> String.format("%s (%s)", t.name(), t.state()))
                    .collect(Collectors.toList());
            
            findings.add(new DiagnosticFinding(
                    "HIGH_WAITING_THREADS",
                    String.format("High number of waiting threads: %d. Common wait patterns: %s", 
                            waitingCount,
                            waitingPatterns.entrySet().stream()
                                    .sorted(Map.Entry.<String, List<ThreadInfo>>comparingByValue(
                                            (a, b) -> Integer.compare(b.size(), a.size())))
                                    .limit(3)
                                    .map(e -> String.format("%s(%d)", e.getKey(), e.getValue().size()))
                                    .collect(Collectors.joining(", "))),
                    waitingCount > 200 ? Severity.MEDIUM : Severity.LOW,
                    topWaitingThreads,
                    "Review thread coordination and consider reducing wait times. Check if waiting is necessary or can be optimized.",
                    Map.of(
                            "waitingCount", waitingCount,
                            "waitingPatterns", waitingPatterns.entrySet().stream()
                                    .collect(Collectors.toMap(
                                            Map.Entry::getKey,
                                            e -> e.getValue().size()
                                    ))
                    )
            ));
        }
        
        return findings;
    }
    
    private List<DiagnosticFinding> detectHotspots(List<ThreadInfo> threads) {
        List<DiagnosticFinding> findings = new ArrayList<>();
        
        // Enhanced hotspot detection - analyze runnable threads for CPU usage patterns
        List<ThreadInfo> runnableThreads = threads.stream()
                .filter(thread -> thread.state() == ThreadState.RUNNABLE)
                .collect(Collectors.toList());
        
        // Find common stack trace patterns in runnable threads (CPU hotspots)
        Map<String, Long> methodCounts = runnableThreads.stream()
                .flatMap(thread -> thread.stackTrace().stream())
                .filter(stackLine -> stackLine.contains("at "))
                .collect(Collectors.groupingBy(
                        stackLine -> extractMethodName(stackLine),
                        Collectors.counting()
                ));
        
        // Also analyze blocked threads for lock contention hotspots
        List<ThreadInfo> blockedThreads = threads.stream()
                .filter(thread -> thread.state() == ThreadState.BLOCKED)
                .collect(Collectors.toList());
        
        Map<String, Long> blockingMethods = blockedThreads.stream()
                .flatMap(thread -> thread.stackTrace().stream())
                .filter(stackLine -> stackLine.contains("at "))
                .collect(Collectors.groupingBy(
                        stackLine -> extractMethodName(stackLine),
                        Collectors.counting()
                ));
        
        // Report CPU hotspots
        methodCounts.entrySet().stream()
                .filter(entry -> entry.getValue() > 3) // Lower threshold for more sensitive detection
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5) // Top 5 hotspots
                .forEach(entry -> {
                    List<String> affectedThreads = runnableThreads.stream()
                            .filter(t -> t.stackTrace().stream().anyMatch(stack -> 
                                    extractMethodName(stack).equals(entry.getKey())))
                            .map(ThreadInfo::name)
                            .collect(Collectors.toList());
                    
                    findings.add(new DiagnosticFinding(
                            "CPU_HOTSPOT",
                            String.format("Method frequently appears in runnable thread stack traces: %s (%d occurrences)", 
                                    entry.getKey(), entry.getValue()),
                            entry.getValue() > 10 ? Severity.HIGH : Severity.MEDIUM,
                            affectedThreads,
                            "Profile and optimize this frequently executed method. Consider caching or algorithm improvements.",
                            Map.of(
                                    "method", entry.getKey(), 
                                    "occurrences", entry.getValue(),
                                    "threadCount", affectedThreads.size()
                            )
                    ));
                });
        
        // Report lock contention hotspots
        blockingMethods.entrySet().stream()
                .filter(entry -> entry.getValue() > 2)
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3) // Top 3 blocking hotspots
                .forEach(entry -> {
                    List<String> affectedThreads = blockedThreads.stream()
                            .filter(t -> t.stackTrace().stream().anyMatch(stack -> 
                                    extractMethodName(stack).equals(entry.getKey())))
                            .map(ThreadInfo::name)
                            .collect(Collectors.toList());
                    
                    findings.add(new DiagnosticFinding(
                            "LOCK_CONTENTION_HOTSPOT",
                            String.format("Method frequently causes thread blocking: %s (%d blocked threads)", 
                                    entry.getKey(), entry.getValue()),
                            Severity.HIGH,
                            affectedThreads,
                            "Review synchronization in this method. Consider reducing lock scope or using lock-free alternatives.",
                            Map.of(
                                    "method", entry.getKey(), 
                                    "blockedCount", entry.getValue(),
                                    "threadCount", affectedThreads.size()
                            )
                    ));
                });
        
        return findings;
    }
    
    private String extractMethodName(String stackLine) {
        // Simple method name extraction from stack trace line
        if (stackLine.contains("(")) {
            return stackLine.substring(stackLine.indexOf("at ") + 3, stackLine.indexOf("("));
        }
        return stackLine;
    }
    
    /**
     * Categorizes a thread into a logical group based on its name and characteristics.
     */
    private String categorizeThread(ThreadInfo thread) {
        String name = thread.name();
        if (name == null) return "Unknown";
        
        String lowerName = name.toLowerCase();
        
        // GC threads
        if (lowerName.contains("gc") || lowerName.contains("concurrent mark") || 
            lowerName.contains("parallel gc") || lowerName.contains("g1")) {
            return "GC";
        }
        
        // HTTP/Web threads
        if (lowerName.contains("http") || lowerName.contains("nio") || 
            lowerName.contains("tomcat") || lowerName.contains("jetty") || 
            lowerName.contains("netty")) {
            return "HTTP/Web";
        }
        
        // Database threads
        if (lowerName.contains("connection") || lowerName.contains("db") || 
            lowerName.contains("hikari") || lowerName.contains("datasource") ||
            lowerName.contains("sql")) {
            return "Database";
        }
        
        // Thread pool threads
        if (lowerName.contains("pool") || lowerName.contains("executor") || 
            lowerName.contains("worker") || lowerName.contains("scheduler")) {
            return "Thread Pool";
        }
        
        // JVM internal threads
        if (lowerName.contains("jvm") || lowerName.contains("vm thread") || 
            lowerName.contains("compiler") || lowerName.contains("sweeper") ||
            lowerName.contains("finalizer") || lowerName.contains("reference handler")) {
            return "JVM Internal";
        }
        
        // Application threads
        if (lowerName.contains("main") || lowerName.contains("application") ||
            lowerName.contains("business") || lowerName.contains("service")) {
            return "Application";
        }
        
        return "Other";
    }
    
    /**
     * Analyzes thread groups for potential issues.
     */
    private List<DiagnosticFinding> analyzeThreadGroups(List<ThreadInfo> threads) {
        List<DiagnosticFinding> findings = new ArrayList<>();
        
        // Group threads by category
        Map<String, List<ThreadInfo>> threadsByGroup = threads.stream()
                .collect(Collectors.groupingBy(this::categorizeThread));
        
        // Check for excessive HTTP threads
        List<ThreadInfo> httpThreads = threadsByGroup.getOrDefault("HTTP/Web", new ArrayList<>());
        if (httpThreads.size() > 200) {
            findings.add(new DiagnosticFinding(
                    "EXCESSIVE_HTTP_THREADS",
                    String.format("High number of HTTP/Web threads: %d", httpThreads.size()),
                    Severity.MEDIUM,
                    httpThreads.stream().limit(10).map(ThreadInfo::name).collect(Collectors.toList()),
                    "Review HTTP thread pool configuration and connection handling",
                    Map.of("threadCount", httpThreads.size(), "category", "HTTP/Web")
            ));
        }
        
        // Check for database connection issues
        List<ThreadInfo> dbThreads = threadsByGroup.getOrDefault("Database", new ArrayList<>());
        long blockedDbThreads = dbThreads.stream()
                .filter(t -> t.state() == ThreadState.BLOCKED)
                .count();
        
        if (blockedDbThreads > 5) {
            findings.add(new DiagnosticFinding(
                    "DATABASE_CONNECTION_CONTENTION",
                    String.format("Multiple database threads are blocked: %d out of %d", 
                            blockedDbThreads, dbThreads.size()),
                    Severity.HIGH,
                    dbThreads.stream()
                            .filter(t -> t.state() == ThreadState.BLOCKED)
                            .map(ThreadInfo::name)
                            .collect(Collectors.toList()),
                    "Check database connection pool configuration and query performance",
                    Map.of("blockedThreads", blockedDbThreads, "totalDbThreads", dbThreads.size())
            ));
        }
        
        return findings;
    }
    
    /**
     * Detects suspicious thread patterns that may indicate problems.
     */
    private List<DiagnosticFinding> detectSuspiciousPatterns(List<ThreadInfo> threads) {
        List<DiagnosticFinding> findings = new ArrayList<>();
        
        // Thread starvation detection
        long blockedThreads = threads.stream()
                .filter(t -> t.state() == ThreadState.BLOCKED)
                .count();
        long runnableThreads = threads.stream()
                .filter(t -> t.state() == ThreadState.RUNNABLE)
                .count();
        
        if (blockedThreads > 0 && runnableThreads < 2) {
            findings.add(new DiagnosticFinding(
                    "THREAD_STARVATION",
                    String.format("Potential thread starvation: %d blocked threads with only %d runnable", 
                            blockedThreads, runnableThreads),
                    Severity.CRITICAL,
                    threads.stream()
                            .filter(t -> t.state() == ThreadState.BLOCKED)
                            .limit(5)
                            .map(ThreadInfo::name)
                            .collect(Collectors.toList()),
                    "Investigate lock contention and consider increasing thread pool sizes",
                    Map.of("blockedThreads", blockedThreads, "runnableThreads", runnableThreads)
            ));
        }
        
        // Excessive blocking pattern
        if (blockedThreads > threads.size() * 0.3) {
            findings.add(new DiagnosticFinding(
                    "EXCESSIVE_BLOCKING",
                    String.format("High percentage of blocked threads: %.1f%% (%d out of %d)", 
                            (blockedThreads * 100.0 / threads.size()), blockedThreads, threads.size()),
                    Severity.HIGH,
                    threads.stream()
                            .filter(t -> t.state() == ThreadState.BLOCKED)
                            .limit(10)
                            .map(ThreadInfo::name)
                            .collect(Collectors.toList()),
                    "Review synchronization mechanisms and reduce lock contention",
                    Map.of("blockingPercentage", (blockedThreads * 100.0 / threads.size()))
            ));
        }
        
        // Detect threads with identical stack traces (potential resource contention)
        Map<String, List<ThreadInfo>> stackTraceGroups = threads.stream()
                .filter(t -> !t.stackTrace().isEmpty())
                .collect(Collectors.groupingBy(
                        t -> t.stackTrace().stream().limit(5).collect(Collectors.joining("|"))
                ));
        
        stackTraceGroups.entrySet().stream()
                .filter(entry -> entry.getValue().size() >= 3)
                .forEach(entry -> {
                    List<ThreadInfo> similarThreads = entry.getValue();
                    findings.add(new DiagnosticFinding(
                            "IDENTICAL_STACK_TRACES",
                            String.format("Multiple threads with identical stack traces: %d threads", 
                                    similarThreads.size()),
                            Severity.MEDIUM,
                            similarThreads.stream()
                                    .map(ThreadInfo::name)
                                    .collect(Collectors.toList()),
                            "Investigate potential resource contention or inefficient synchronization",
                            Map.of("threadCount", similarThreads.size(), 
                                  "stackTrace", similarThreads.get(0).stackTrace().stream()
                                          .limit(3)
                                          .collect(Collectors.toList()))
                    ));
                });
        
        return findings;
    }
}