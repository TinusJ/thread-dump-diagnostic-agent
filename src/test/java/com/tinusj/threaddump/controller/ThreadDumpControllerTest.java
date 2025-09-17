package com.tinusj.threaddump.controller;

import com.tinusj.threaddump.enums.ReportFormat;
import com.tinusj.threaddump.enums.ReportStatus;
import com.tinusj.threaddump.model.DiagnosticReport;
import com.tinusj.threaddump.service.DiagnosticService;
import com.tinusj.threaddump.service.ReportFormatterService;
import com.tinusj.threaddump.skill.ThreadDumpAnalysisSkill;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ThreadDumpController.
 */
@ExtendWith(MockitoExtension.class)
class ThreadDumpControllerTest {
    
    @Mock
    private DiagnosticService diagnosticService;
    
    @Mock
    private ReportFormatterService reportFormatterService;
    
    @Mock
    private ThreadDumpAnalysisSkill mcpSkill;
    
    @InjectMocks
    private ThreadDumpController controller;
    
    @Test
    void analyzeThreadDumpText_ShouldReturnReport_WhenGivenValidContent() {
        // Given
        String threadDumpContent = "Sample thread dump content";
        DiagnosticReport mockReport = new DiagnosticReport(
                "test-id",
                LocalDateTime.now(),
                "text-input",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                ReportStatus.COMPLETED,
                "Test summary"
        );
        
        when(diagnosticService.analyzeThreadDump(anyString(), anyString())).thenReturn(mockReport);
        when(reportFormatterService.formatReport(any(), any())).thenReturn("{\"id\":\"test-id\"}");
        
        // When
        ResponseEntity<String> response = controller.analyzeThreadDumpText(threadDumpContent, ReportFormat.JSON);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("{\"id\":\"test-id\"}");
    }
    
    @Test
    void analyzeThreadDumpText_ShouldReturnBadRequest_WhenGivenEmptyContent() {
        // When
        ResponseEntity<String> response = controller.analyzeThreadDumpText("", ReportFormat.JSON);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Thread dump content cannot be empty");
    }
}