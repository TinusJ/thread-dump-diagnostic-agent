package com.tinusj.threaddump.controller;

import com.tinusj.threaddump.model.JavaProcess;
import com.tinusj.threaddump.service.DiagnosticService;
import com.tinusj.threaddump.service.JavaProcessService;
import com.tinusj.threaddump.service.ReportFormatterService;
import com.tinusj.threaddump.skill.ThreadDumpAnalysisSkill;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Tests for Java process-related endpoints in ThreadDumpController.
 */
@ExtendWith(MockitoExtension.class)
class JavaProcessControllerTest {

    @Mock
    private JavaProcessService javaProcessService;

    @Mock
    private ThreadDumpAnalysisSkill mcpSkill;

    @Mock
    private DiagnosticService diagnosticService;

    @Mock
    private ReportFormatterService reportFormatterService;
    
    private ThreadDumpController controller;
    
    @BeforeEach
    void setUp() {
        controller = new ThreadDumpController(diagnosticService, reportFormatterService, mcpSkill, javaProcessService);
    }

    @Test
    void testGetRunningJavaProcesses() {
        // Given
        JavaProcess process1 = new JavaProcess(1234L, "com.example.App1", "App1", "-Xmx1g", "arg1 arg2");
        JavaProcess process2 = new JavaProcess(5678L, "com.example.App2", "App2", "-Xmx2g", "");
        
        when(javaProcessService.getRunningJavaProcesses()).thenReturn(Arrays.asList(process1, process2));

        // When
        ResponseEntity<List<JavaProcess>> response = controller.getRunningJavaProcesses();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(1234L, response.getBody().get(0).pid());
        assertEquals("com.example.App1", response.getBody().get(0).mainClass());
    }

    @Test
    void testGetJavaProcessByPid() {
        // Given
        JavaProcess process = new JavaProcess(1234L, "com.example.App", "App", "-Xmx1g", "arg1");
        when(javaProcessService.getJavaProcessByPid(1234L)).thenReturn(process);

        // When
        ResponseEntity<JavaProcess> response = controller.getJavaProcessByPid(1234L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1234L, response.getBody().pid());
        assertEquals("com.example.App", response.getBody().mainClass());
    }

    @Test
    void testGetJavaProcessByPidNotFound() {
        // Given
        when(javaProcessService.getJavaProcessByPid(anyLong())).thenReturn(null);

        // When
        ResponseEntity<JavaProcess> response = controller.getJavaProcessByPid(999999L);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testMcpGetJavaProcesses() {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("format", "JSON");
        
        String expectedResponse = "{\"javaProcesses\": []}";
        when(mcpSkill.handleGetJavaProcesses(any())).thenReturn(expectedResponse);

        // When
        ResponseEntity<String> response = controller.mcpGetJavaProcesses(arguments);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }
}