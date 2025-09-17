package com.tinusj.threaddump.service.impl;

import com.tinusj.threaddump.model.DiagnosticReport;
import com.tinusj.threaddump.model.ThreadDumpRequest;
import com.tinusj.threaddump.model.ThreadDumpResponse;
import com.tinusj.threaddump.service.DiagnosticAnalysisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ThreadDumpServiceImplTest {

    @Mock
    private DiagnosticAnalysisService diagnosticAnalysisService;

    private ThreadDumpServiceImpl threadDumpService;

    @BeforeEach
    void setUp() {
        threadDumpService = new ThreadDumpServiceImpl(diagnosticAnalysisService);
    }

    @Test
    void generateThreadDump_ShouldReturnValidResponse() {
        // Given
        ThreadDumpRequest request = new ThreadDumpRequest("12345", 30, true, true);
        when(diagnosticAnalysisService.analyzeThreadDump(any())).thenReturn(new ArrayList<>());

        // When
        ThreadDumpResponse response = threadDumpService.generateThreadDump(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.dumpId());
        assertNotNull(response.timestamp());
        assertEquals("12345", response.processId());
        assertTrue(response.totalThreads() > 0);
        assertNotNull(response.threads());
        assertNotNull(response.diagnostics());
    }

    @Test
    void generateThreadDump_WithoutAnalysis_ShouldReturnEmptyDiagnostics() {
        // Given
        ThreadDumpRequest request = new ThreadDumpRequest("12345", 30, true, false);

        // When
        ThreadDumpResponse response = threadDumpService.generateThreadDump(request);

        // Then
        assertNotNull(response);
        assertTrue(response.diagnostics().isEmpty());
    }

    @Test
    void getThreadDump_WithValidId_ShouldReturnResponse() {
        // Given
        ThreadDumpRequest request = new ThreadDumpRequest("12345", 30, true, false);
        ThreadDumpResponse originalResponse = threadDumpService.generateThreadDump(request);

        // When
        ThreadDumpResponse retrievedResponse = threadDumpService.getThreadDump(originalResponse.dumpId());

        // Then
        assertNotNull(retrievedResponse);
        assertEquals(originalResponse.dumpId(), retrievedResponse.dumpId());
        assertEquals(originalResponse.processId(), retrievedResponse.processId());
    }

    @Test
    void getThreadDump_WithInvalidId_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            threadDumpService.getThreadDump("invalid-id"));
    }

    @Test
    void getAllThreadDumps_ShouldReturnAllCreatedDumps() {
        // Given
        ThreadDumpRequest request1 = new ThreadDumpRequest("12345", 30, true, false);
        ThreadDumpRequest request2 = new ThreadDumpRequest("67890", 30, true, false);
        
        threadDumpService.generateThreadDump(request1);
        threadDumpService.generateThreadDump(request2);

        // When
        List<ThreadDumpResponse> allDumps = threadDumpService.getAllThreadDumps();

        // Then
        assertEquals(2, allDumps.size());
    }

    @Test
    void deleteThreadDump_WithValidId_ShouldRemoveDump() {
        // Given
        ThreadDumpRequest request = new ThreadDumpRequest("12345", 30, true, false);
        ThreadDumpResponse response = threadDumpService.generateThreadDump(request);

        // When
        threadDumpService.deleteThreadDump(response.dumpId());

        // Then
        assertThrows(IllegalArgumentException.class, () -> 
            threadDumpService.getThreadDump(response.dumpId()));
    }

    @Test
    void deleteThreadDump_WithInvalidId_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            threadDumpService.deleteThreadDump("invalid-id"));
    }
}