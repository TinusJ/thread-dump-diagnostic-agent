package com.tinusj.threaddump.service;

import com.tinusj.threaddump.model.DiagnosticFinding;
import com.tinusj.threaddump.model.DiagnosticReport;
import com.tinusj.threaddump.model.ThreadStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DiagnosticService.
 */
@ExtendWith(MockitoExtension.class)
class DiagnosticServiceTest {
    
    @Mock
    private ThreadDumpAnalyzer threadDumpAnalyzer;
    
    private DiagnosticService diagnosticService;
    
    @BeforeEach
    void setUp() {
        diagnosticService = new DiagnosticService(threadDumpAnalyzer);
    }
    
    @Test
    void analyzThreadDump_ShouldReturnValidReport_WhenGivenValidInput() {
        // Given
        String threadDumpContent = "Sample thread dump content";
        String source = "test-source";
        
        ThreadStatistics mockStats = ThreadStatistics.builder()
                .totalThreads(10)
                .blockedThreads(1)
                .waitingThreads(2)
                .runnableThreads(7)
                .build();
        
        List<DiagnosticFinding> mockFindings = new ArrayList<>();
        mockFindings.add(DiagnosticFinding.builder()
                .type("TEST_FINDING")
                .description("Test finding")
                .severity(DiagnosticFinding.Severity.LOW)
                .build());
        
        when(threadDumpAnalyzer.analyzeStatistics(anyString())).thenReturn(mockStats);
        when(threadDumpAnalyzer.analyzeFindings(anyString())).thenReturn(mockFindings);
        
        // When
        DiagnosticReport report = diagnosticService.analyzThreadDump(threadDumpContent, source);
        
        // Then
        assertThat(report).isNotNull();
        assertThat(report.getId()).isNotNull();
        assertThat(report.getSource()).isEqualTo(source);
        assertThat(report.getStatus()).isEqualTo("COMPLETED");
        assertThat(report.getStatistics()).isEqualTo(mockStats);
        assertThat(report.getFindings()).hasSize(1);
        assertThat(report.getSuggestedFixes()).isNotEmpty();
        assertThat(report.getSummary()).isNotNull();
    }
    
    @Test
    void analyzThreadDump_ShouldHandleException_WhenAnalysisFails() {
        // Given
        String threadDumpContent = "Invalid content";
        String source = "test-source";
        
        when(threadDumpAnalyzer.analyzeStatistics(anyString()))
                .thenThrow(new RuntimeException("Analysis failed"));
        
        // When
        DiagnosticReport report = diagnosticService.analyzThreadDump(threadDumpContent, source);
        
        // Then
        assertThat(report).isNotNull();
        assertThat(report.getStatus()).isEqualTo("ERROR");
        assertThat(report.getSummary()).contains("Analysis failed");
    }
}