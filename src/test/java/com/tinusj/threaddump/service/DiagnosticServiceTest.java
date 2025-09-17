package com.tinusj.threaddump.service;

import com.tinusj.threaddump.enums.Severity;
import com.tinusj.threaddump.model.DiagnosticFinding;
import com.tinusj.threaddump.model.DiagnosticReport;
import com.tinusj.threaddump.model.ThreadStatistics;
import com.tinusj.threaddump.service.impl.DiagnosticServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DiagnosticServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class DiagnosticServiceTest {
    
    @Mock
    private ThreadDumpAnalyzer threadDumpAnalyzer;
    
    private DiagnosticService diagnosticService;
    
    @BeforeEach
    void setUp() {
        diagnosticService = new DiagnosticServiceImpl(threadDumpAnalyzer);
    }
    
    @Test
    void analyzeThreadDump_ShouldReturnValidReport_WhenGivenValidInput() {
        // Given
        String threadDumpContent = "Sample thread dump content";
        String source = "test-source";
        
        ThreadStatistics mockStats = new ThreadStatistics(
                10,
                null,
                0,
                1,
                2,
                7,
                Map.of("Application", 5, "HTTP/Web", 3, "Other", 2)
        );
        
        List<DiagnosticFinding> mockFindings = new ArrayList<>();
        mockFindings.add(new DiagnosticFinding(
                "TEST_FINDING",
                "Test finding",
                Severity.LOW,
                null,
                null,
                null
        ));
        
        when(threadDumpAnalyzer.analyzeStatistics(anyString())).thenReturn(mockStats);
        when(threadDumpAnalyzer.analyzeFindings(anyString())).thenReturn(mockFindings);
        
        // When
        DiagnosticReport report = diagnosticService.analyzeThreadDump(threadDumpContent, source);
        
        // Then
        assertThat(report).isNotNull();
        assertThat(report.id()).isNotNull();
        assertThat(report.source()).isEqualTo(source);
        assertThat(report.status().toString()).isEqualTo("COMPLETED");
        assertThat(report.statistics()).isEqualTo(mockStats);
        assertThat(report.findings()).hasSize(1);
        assertThat(report.suggestedFixes()).isNotEmpty();
        assertThat(report.summary()).isNotNull();
    }
    
    @Test
    void analyzeThreadDump_ShouldHandleException_WhenAnalysisFails() {
        // Given
        String threadDumpContent = "Invalid content";
        String source = "test-source";
        
        when(threadDumpAnalyzer.analyzeStatistics(anyString()))
                .thenThrow(new RuntimeException("Analysis failed"));
        
        // When
        DiagnosticReport report = diagnosticService.analyzeThreadDump(threadDumpContent, source);
        
        // Then
        assertThat(report).isNotNull();
        assertThat(report.status().toString()).isEqualTo("ERROR");
        assertThat(report.summary()).contains("Analysis failed");
    }
}