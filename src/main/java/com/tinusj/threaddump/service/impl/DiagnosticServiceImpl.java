package com.tinusj.threaddump.service.impl;

import com.tinusj.threaddump.enums.ReportStatus;
import com.tinusj.threaddump.enums.Severity;
import com.tinusj.threaddump.model.DiagnosticFinding;
import com.tinusj.threaddump.model.DiagnosticReport;
import com.tinusj.threaddump.model.ThreadStatistics;
import com.tinusj.threaddump.service.DiagnosticService;
import com.tinusj.threaddump.service.ThreadDumpAnalyzer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.UUID;

/**
 * Implementation of DiagnosticService for orchestrating thread dump diagnostic analysis.
 */
@Service
@Slf4j
public class DiagnosticServiceImpl implements DiagnosticService {
    
    private final ThreadDumpAnalyzer threadDumpAnalyzer;
    
    public DiagnosticServiceImpl(ThreadDumpAnalyzer threadDumpAnalyzer) {
        this.threadDumpAnalyzer = threadDumpAnalyzer;
    }
    
    @Override
    public DiagnosticReport analyzeThreadDump(String threadDumpContent, String source) {
        log.info("Starting thread dump analysis for source: {}", source);
        
        String reportId = UUID.randomUUID().toString();
        
        try {
            // Generate statistics
            ThreadStatistics statistics = threadDumpAnalyzer.analyzeStatistics(threadDumpContent);
            log.debug("Generated statistics for {} threads", statistics.totalThreads());
            
            // Generate findings
            List<DiagnosticFinding> findings = threadDumpAnalyzer.analyzeFindings(threadDumpContent);
            log.debug("Generated {} diagnostic findings", findings.size());
            
            // Generate suggested fixes
            List<String> suggestedFixes = generateSuggestedFixes(findings, statistics);
            
            // Generate summary
            String summary = generateSummary(statistics, findings);
            
            DiagnosticReport report = new DiagnosticReport(
                    reportId,
                    LocalDateTime.now(),
                    source,
                    statistics,
                    findings,
                    suggestedFixes,
                    ReportStatus.COMPLETED,
                    summary
            );
            
            log.info("Thread dump analysis completed for source: {}, report ID: {}", source, reportId);
            return report;
            
        } catch (Exception e) {
            log.error("Error analyzing thread dump for source: {}", source, e);
            
            return new DiagnosticReport(
                    reportId,
                    LocalDateTime.now(),
                    source,
                    null,
                    new ArrayList<>(),
                    List.of("Review thread dump format and content"),
                    ReportStatus.ERROR,
                    "Analysis failed: " + e.getMessage()
            );
        }
    }
    
    private List<String> generateSuggestedFixes(List<DiagnosticFinding> findings, ThreadStatistics statistics) {
        List<String> fixes = new ArrayList<>();
        
        if (statistics.totalThreads() > 1000) {
            fixes.add("Consider implementing thread pooling to reduce the total number of threads");
        }
        
        if (statistics.blockedThreads() > 10) {
            fixes.add("Review synchronization mechanisms to reduce thread blocking");
            fixes.add("Consider using lock-free data structures or reducing lock scope");
        }
        
        if (statistics.waitingThreads() > 50) {
            fixes.add("Optimize thread coordination and reduce unnecessary waiting");
            fixes.add("Review timeout values for blocking operations");
        }
        
        // Add specific fixes based on findings
        for (DiagnosticFinding finding : findings) {
            switch (finding.type()) {
                case "POTENTIAL_DEADLOCK" -> {
                    fixes.add("Implement consistent lock ordering across all threads");
                    fixes.add("Use timeout-based locking mechanisms (tryLock with timeout)");
                    fixes.add("Consider using higher-level concurrency utilities like java.util.concurrent");
                    fixes.add("Review lock acquisition patterns and minimize lock holding time");
                }
                case "HIGH_THREAD_COUNT" -> {
                    fixes.add("Implement thread pooling with appropriate pool sizes");
                    fixes.add("Review thread lifecycle management and ensure proper cleanup");
                    fixes.add("Consider using virtual threads (Project Loom) if available");
                }
                case "CPU_HOTSPOT", "HOTSPOT" -> {
                    fixes.add("Profile and optimize frequently called methods");
                    fixes.add("Consider caching results for expensive operations");
                    fixes.add("Review algorithms for performance improvements");
                    fixes.add("Consider parallel processing for CPU-intensive tasks");
                }
                case "LOCK_CONTENTION_HOTSPOT" -> {
                    fixes.add("Reduce synchronization scope and use finer-grained locking");
                    fixes.add("Consider lock-free data structures and algorithms");
                    fixes.add("Use concurrent collections instead of synchronized collections");
                    fixes.add("Implement read-write locks where appropriate");
                }
                case "HIGH_BLOCKED_THREADS" -> {
                    fixes.add("Analyze lock contention and reduce synchronization overhead");
                    fixes.add("Consider using non-blocking algorithms and data structures");
                    fixes.add("Review critical sections and minimize lock holding time");
                }
                case "THREAD_STARVATION" -> {
                    fixes.add("Increase thread pool sizes or use adaptive sizing");
                    fixes.add("Review thread priorities and scheduling");
                    fixes.add("Implement fair locking mechanisms");
                    fixes.add("Consider using separate thread pools for different task types");
                }
                case "EXCESSIVE_HTTP_THREADS" -> {
                    fixes.add("Tune HTTP connector thread pool configuration");
                    fixes.add("Implement connection pooling and keep-alive optimization");
                    fixes.add("Review request processing efficiency");
                }
                case "DATABASE_CONNECTION_CONTENTION" -> {
                    fixes.add("Increase database connection pool size");
                    fixes.add("Optimize database queries and reduce query execution time");
                    fixes.add("Implement connection leak detection and prevention");
                    fixes.add("Consider using read replicas for read-heavy workloads");
                }
                case "EXCESSIVE_BLOCKING" -> {
                    fixes.add("Review synchronization patterns and reduce lock usage");
                    fixes.add("Implement asynchronous processing where possible");
                    fixes.add("Use message queues for decoupling components");
                }
                case "IDENTICAL_STACK_TRACES" -> {
                    fixes.add("Investigate shared resource bottlenecks");
                    fixes.add("Consider load balancing or partitioning strategies");
                    fixes.add("Review serialization points in the application");
                }
            }
        }
        
        // Add thread group specific recommendations
        if (statistics.threadGroups() != null) {
            statistics.threadGroups().forEach((group, count) -> {
                if (count > 100) {
                    fixes.add(String.format("Review %s thread group usage - %d threads may be excessive", group, count));
                }
            });
        }
        
        if (fixes.isEmpty()) {
            fixes.add("Thread dump appears healthy. Continue monitoring for performance trends.");
            fixes.add("Consider implementing thread dump collection automation for trend analysis.");
        }
        
        return fixes;
    }
    
    private String generateSummary(ThreadStatistics statistics, List<DiagnosticFinding> findings) {
        StringBuilder summary = new StringBuilder();
        
        summary.append(String.format("Analyzed %d threads. ", statistics.totalThreads()));
        
        if (statistics.blockedThreads() > 0) {
            summary.append(String.format("%d blocked, ", statistics.blockedThreads()));
        }
        
        if (statistics.waitingThreads() > 0) {
            summary.append(String.format("%d waiting, ", statistics.waitingThreads()));
        }
        
        summary.append(String.format("%d runnable. ", statistics.runnableThreads()));
        
        // Add thread group summary
        if (statistics.threadGroups() != null && !statistics.threadGroups().isEmpty()) {
            String topGroups = statistics.threadGroups().entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(3)
                    .map(e -> String.format("%s(%d)", e.getKey(), e.getValue()))
                    .collect(Collectors.joining(", "));
            summary.append(String.format("Top groups: %s. ", topGroups));
        }
        
        if (findings.isEmpty()) {
            summary.append("No significant issues detected.");
        } else {
            long criticalCount = findings.stream()
                    .filter(f -> f.severity() == Severity.CRITICAL)
                    .count();
            long highCount = findings.stream()
                    .filter(f -> f.severity() == Severity.HIGH)
                    .count();
            long mediumCount = findings.stream()
                    .filter(f -> f.severity() == Severity.MEDIUM)
                    .count();
            
            summary.append(String.format("Found %d issues", findings.size()));
            
            if (criticalCount > 0) {
                summary.append(String.format(" (%d critical", criticalCount));
                if (highCount > 0 || mediumCount > 0) {
                    summary.append(String.format(", %d high, %d medium", highCount, mediumCount));
                }
                summary.append(").");
            } else if (highCount > 0) {
                summary.append(String.format(" (%d high", highCount));
                if (mediumCount > 0) {
                    summary.append(String.format(", %d medium", mediumCount));
                }
                summary.append(").");
            } else if (mediumCount > 0) {
                summary.append(String.format(" (%d medium).", mediumCount));
            } else {
                summary.append(" (all low severity).");
            }
        }
        
        return summary.toString();
    }
}