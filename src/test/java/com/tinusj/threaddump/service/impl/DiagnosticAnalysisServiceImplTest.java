package com.tinusj.threaddump.service.impl;

import com.tinusj.threaddump.enums.DiagnosticSeverity;
import com.tinusj.threaddump.enums.DiagnosticType;
import com.tinusj.threaddump.enums.ThreadState;
import com.tinusj.threaddump.model.DiagnosticReport;
import com.tinusj.threaddump.model.ThreadInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiagnosticAnalysisServiceImplTest {

    private DiagnosticAnalysisServiceImpl diagnosticService;

    @BeforeEach
    void setUp() {
        diagnosticService = new DiagnosticAnalysisServiceImpl();
    }

    @Test
    void detectDeadlocks_WithBlockedThreads_ShouldReturnDeadlockReport() {
        // Given
        List<ThreadInfo> threads = List.of(
            new ThreadInfo(1L, "thread-1", ThreadState.BLOCKED, 1000L, 5000L, 2000L, 
                          "java.lang.Object", "owner-thread", 2L, List.of()),
            new ThreadInfo(2L, "thread-2", ThreadState.BLOCKED, 1500L, 3000L, 1000L, 
                          "java.lang.Object", "owner-thread", 3L, List.of()),
            new ThreadInfo(3L, "thread-3", ThreadState.RUNNABLE, 2000L, 0L, 0L, 
                          null, null, null, List.of())
        );

        // When
        DiagnosticReport report = diagnosticService.detectDeadlocks(threads);

        // Then
        assertNotNull(report);
        assertEquals(DiagnosticType.DEADLOCK, report.type());
        assertEquals(DiagnosticSeverity.MEDIUM, report.severity());
        assertEquals(2, report.affectedThreads().size());
        assertTrue(report.description().contains("2 blocked threads"));
        assertNotNull(report.recommendations());
        assertTrue(report.recommendations().size() > 0);
    }

    @Test
    void detectDeadlocks_WithNoBlockedThreads_ShouldReturnNull() {
        // Given
        List<ThreadInfo> threads = List.of(
            new ThreadInfo(1L, "thread-1", ThreadState.RUNNABLE, 1000L, 0L, 0L, 
                          null, null, null, List.of()),
            new ThreadInfo(2L, "thread-2", ThreadState.WAITING, 1500L, 0L, 5000L, 
                          null, null, null, List.of())
        );

        // When
        DiagnosticReport report = diagnosticService.detectDeadlocks(threads);

        // Then
        assertNull(report);
    }

    @Test
    void analyzePerformance_WithHighCpuThreads_ShouldReturnPerformanceReport() {
        // Given
        List<ThreadInfo> threads = List.of(
            new ThreadInfo(1L, "thread-1", ThreadState.RUNNABLE, 2_000_000_000L, 0L, 0L, 
                          null, null, null, List.of()),
            new ThreadInfo(2L, "thread-2", ThreadState.RUNNABLE, 500_000_000L, 0L, 0L, 
                          null, null, null, List.of()),
            new ThreadInfo(3L, "thread-3", ThreadState.RUNNABLE, 3_000_000_000L, 0L, 0L, 
                          null, null, null, List.of())
        );

        // When
        DiagnosticReport report = diagnosticService.analyzePerformance(threads);

        // Then
        assertNotNull(report);
        assertEquals(DiagnosticType.HIGH_CPU_USAGE, report.type());
        assertEquals(DiagnosticSeverity.HIGH, report.severity());
        assertEquals(2, report.affectedThreads().size());
        assertTrue(report.description().contains("2 threads with high CPU"));
        assertNotNull(report.recommendations());
    }

    @Test
    void analyzePerformance_WithLowCpuThreads_ShouldReturnNull() {
        // Given
        List<ThreadInfo> threads = List.of(
            new ThreadInfo(1L, "thread-1", ThreadState.RUNNABLE, 500_000_000L, 0L, 0L, 
                          null, null, null, List.of()),
            new ThreadInfo(2L, "thread-2", ThreadState.RUNNABLE, 200_000_000L, 0L, 0L, 
                          null, null, null, List.of())
        );

        // When
        DiagnosticReport report = diagnosticService.analyzePerformance(threads);

        // Then
        assertNull(report);
    }

    @Test
    void analyzeThreadDump_ShouldReturnCombinedReports() {
        // Given
        List<ThreadInfo> threads = List.of(
            new ThreadInfo(1L, "thread-1", ThreadState.BLOCKED, 2_000_000_000L, 5000L, 2000L, 
                          "java.lang.Object", "owner-thread", 2L, List.of()),
            new ThreadInfo(2L, "thread-2", ThreadState.RUNNABLE, 3_000_000_000L, 0L, 0L, 
                          null, null, null, List.of()),
            new ThreadInfo(3L, "thread-3", ThreadState.RUNNABLE, 500_000_000L, 0L, 0L, 
                          null, null, null, List.of())
        );

        // When
        List<DiagnosticReport> reports = diagnosticService.analyzeThreadDump(threads);

        // Then
        assertEquals(2, reports.size());
        assertTrue(reports.stream().anyMatch(r -> r.type() == DiagnosticType.DEADLOCK));
        assertTrue(reports.stream().anyMatch(r -> r.type() == DiagnosticType.HIGH_CPU_USAGE));
    }

    @Test
    void generateRecommendations_ShouldCombineAllRecommendations() {
        // Given
        List<ThreadInfo> threads = List.of(
            new ThreadInfo(1L, "thread-1", ThreadState.BLOCKED, 2_000_000_000L, 5000L, 2000L, 
                          "java.lang.Object", "owner-thread", 2L, List.of())
        );
        List<DiagnosticReport> reports = diagnosticService.analyzeThreadDump(threads);

        // When
        List<String> recommendations = diagnosticService.generateRecommendations(reports);

        // Then
        assertNotNull(recommendations);
        assertTrue(recommendations.size() > 0);
        assertTrue(recommendations.contains("Regular thread dump analysis"));
        assertTrue(recommendations.contains("Monitor application performance metrics"));
    }
}