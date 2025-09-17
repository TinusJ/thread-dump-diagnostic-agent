package com.tinusj.threaddump.service.impl;

import com.tinusj.threaddump.enums.DiagnosticSeverity;
import com.tinusj.threaddump.enums.DiagnosticType;
import com.tinusj.threaddump.enums.ThreadState;
import com.tinusj.threaddump.model.DiagnosticReport;
import com.tinusj.threaddump.model.ThreadInfo;
import com.tinusj.threaddump.service.DiagnosticAnalysisService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DiagnosticAnalysisServiceImpl implements DiagnosticAnalysisService {

    @Override
    public List<DiagnosticReport> analyzeThreadDump(List<ThreadInfo> threads) {
        List<DiagnosticReport> reports = new ArrayList<>();
        
        DiagnosticReport deadlockReport = detectDeadlocks(threads);
        if (deadlockReport != null) {
            reports.add(deadlockReport);
        }
        
        DiagnosticReport performanceReport = analyzePerformance(threads);
        if (performanceReport != null) {
            reports.add(performanceReport);
        }
        
        return reports;
    }

    @Override
    public DiagnosticReport detectDeadlocks(List<ThreadInfo> threads) {
        List<ThreadInfo> blockedThreads = threads.stream()
            .filter(thread -> thread.state() == ThreadState.BLOCKED)
            .collect(Collectors.toList());
        
        if (blockedThreads.isEmpty()) {
            return null;
        }

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("blockedThreadsCount", blockedThreads.size());
        metrics.put("totalThreadsCount", threads.size());
        
        List<String> recommendations = List.of(
            "Review thread synchronization mechanisms",
            "Consider using concurrent collections",
            "Analyze lock contention patterns",
            "Implement timeout mechanisms for locks"
        );

        return new DiagnosticReport(
            UUID.randomUUID().toString(),
            LocalDateTime.now(),
            DiagnosticType.DEADLOCK,
            blockedThreads.size() > 5 ? DiagnosticSeverity.HIGH : DiagnosticSeverity.MEDIUM,
            String.format("Detected %d blocked threads potentially indicating deadlock conditions", blockedThreads.size()),
            blockedThreads,
            metrics,
            recommendations
        );
    }

    @Override
    public DiagnosticReport analyzePerformance(List<ThreadInfo> threads) {
        long highCpuThreads = threads.stream()
            .filter(thread -> thread.cpuTime() != null && thread.cpuTime() > 1_000_000_000L) // 1 second in nanoseconds
            .count();
        
        if (highCpuThreads == 0) {
            return null;
        }

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("highCpuThreadsCount", highCpuThreads);
        metrics.put("totalThreadsCount", threads.size());
        metrics.put("cpuUsageThreshold", "1 second");
        
        List<ThreadInfo> affectedThreads = threads.stream()
            .filter(thread -> thread.cpuTime() != null && thread.cpuTime() > 1_000_000_000L)
            .collect(Collectors.toList());

        List<String> recommendations = List.of(
            "Profile CPU-intensive operations",
            "Consider optimizing algorithms in high-CPU threads",
            "Review thread pool sizing",
            "Implement CPU usage monitoring"
        );

        return new DiagnosticReport(
            UUID.randomUUID().toString(),
            LocalDateTime.now(),
            DiagnosticType.HIGH_CPU_USAGE,
            highCpuThreads > 10 ? DiagnosticSeverity.CRITICAL : DiagnosticSeverity.HIGH,
            String.format("Detected %d threads with high CPU usage", highCpuThreads),
            affectedThreads,
            metrics,
            recommendations
        );
    }

    @Override
    public List<String> generateRecommendations(List<DiagnosticReport> reports) {
        List<String> allRecommendations = new ArrayList<>();
        
        for (DiagnosticReport report : reports) {
            if (report.recommendations() != null) {
                allRecommendations.addAll(report.recommendations());
            }
        }
        
        // Add general recommendations
        allRecommendations.add("Regular thread dump analysis");
        allRecommendations.add("Monitor application performance metrics");
        allRecommendations.add("Implement proper error handling");
        
        return allRecommendations;
    }
}