package com.tinusj.threaddump.controller;

import com.tinusj.threaddump.enums.ReportFormat;
import com.tinusj.threaddump.enums.ReportStatus;
import com.tinusj.threaddump.model.DiagnosticReport;
import com.tinusj.threaddump.service.DiagnosticService;
import com.tinusj.threaddump.service.JavaProcessService;
import com.tinusj.threaddump.service.ReportFormatterService;
import com.tinusj.threaddump.service.ThreadDumpGenerationService;
import com.tinusj.threaddump.skill.ThreadDumpAnalysisSkill;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ThreadDumpController.class)
class ThreadDumpGenerationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DiagnosticService diagnosticService;

    @MockBean
    private ReportFormatterService reportFormatterService;

    @MockBean
    private ThreadDumpAnalysisSkill mcpSkill;

    @MockBean
    private JavaProcessService javaProcessService;

    @MockBean
    private ThreadDumpGenerationService threadDumpGenerationService;

    @Test
    void testGenerateThreadDump_Success() throws Exception {
        // Given
        long pid = 1234L;
        String mockThreadDump = "Mock thread dump content\nThread details...";
        
        when(threadDumpGenerationService.isAvailable()).thenReturn(true);
        when(threadDumpGenerationService.generateThreadDump(pid)).thenReturn(mockThreadDump);

        // When & Then
        mockMvc.perform(post("/thread-dump/generate/{pid}", pid)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain"))
                .andExpect(content().string(mockThreadDump))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"threaddump_1234.txt\""));
    }

    @Test
    void testGenerateThreadDump_ServiceUnavailable() throws Exception {
        // Given
        long pid = 1234L;
        when(threadDumpGenerationService.isAvailable()).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/thread-dump/generate/{pid}", pid)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string("Thread dump generation is not available on this system"));
    }

    @Test
    void testGenerateThreadDump_InvalidPid() throws Exception {
        // Given
        long pid = 999999L;
        when(threadDumpGenerationService.isAvailable()).thenReturn(true);
        when(threadDumpGenerationService.generateThreadDump(pid))
                .thenThrow(new IllegalArgumentException("PID 999999 is not a valid Java process or not found"));

        // When & Then
        mockMvc.perform(post("/thread-dump/generate/{pid}", pid)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid PID: PID 999999 is not a valid Java process or not found"));
    }

    @Test
    void testGenerateAndAnalyzeThreadDump_Success() throws Exception {
        // Given
        long pid = 1234L;
        String mockThreadDump = "Mock thread dump content\nThread details...";
        DiagnosticReport mockReport = new DiagnosticReport(
                "test-report-id",
                LocalDateTime.now(),
                "pid-1234",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                ReportStatus.COMPLETED,
                "Test summary"
        );
        String formattedReport = "{\"reportId\":\"test-report-id\"}";
        
        when(threadDumpGenerationService.isAvailable()).thenReturn(true);
        when(threadDumpGenerationService.generateThreadDump(pid)).thenReturn(mockThreadDump);
        when(diagnosticService.analyzeThreadDump(eq(mockThreadDump), eq("pid-1234"))).thenReturn(mockReport);
        when(reportFormatterService.formatReport(eq(mockReport), eq(ReportFormat.JSON))).thenReturn(formattedReport);

        // When & Then
        mockMvc.perform(post("/thread-dump/generate-and-analyze/{pid}", pid)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string(formattedReport))
                .andExpect(header().string("Content-Disposition", 
                        "attachment; filename=\"analysis_1234_test-report-id.json\""));
    }

    @Test
    void testGenerateAndAnalyzeThreadDump_ServiceUnavailable() throws Exception {
        // Given
        long pid = 1234L;
        when(threadDumpGenerationService.isAvailable()).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/thread-dump/generate-and-analyze/{pid}", pid)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string("Thread dump generation is not available on this system"));
    }

    @Test
    void testGenerateAndAnalyzeThreadDump_WithCustomFormat() throws Exception {
        // Given
        long pid = 1234L;
        String mockThreadDump = "Mock thread dump content\nThread details...";
        DiagnosticReport mockReport = new DiagnosticReport(
                "test-report-id",
                LocalDateTime.now(),
                "pid-1234",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                ReportStatus.COMPLETED,
                "Test summary"
        );
        String formattedReport = "<report><id>test-report-id</id></report>";
        
        when(threadDumpGenerationService.isAvailable()).thenReturn(true);
        when(threadDumpGenerationService.generateThreadDump(pid)).thenReturn(mockThreadDump);
        when(diagnosticService.analyzeThreadDump(eq(mockThreadDump), eq("pid-1234"))).thenReturn(mockReport);
        when(reportFormatterService.formatReport(eq(mockReport), eq(ReportFormat.XML))).thenReturn(formattedReport);

        // When & Then
        mockMvc.perform(post("/thread-dump/generate-and-analyze/{pid}?format=XML", pid)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/xml"))
                .andExpect(content().string(formattedReport));
    }
}