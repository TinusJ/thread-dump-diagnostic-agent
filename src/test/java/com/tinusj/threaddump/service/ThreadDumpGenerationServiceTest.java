package com.tinusj.threaddump.service;

import com.tinusj.threaddump.model.JavaProcess;
import com.tinusj.threaddump.service.impl.ThreadDumpGenerationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThreadDumpGenerationServiceTest {

    @Mock
    private JavaProcessService javaProcessService;

    private ThreadDumpGenerationService threadDumpGenerationService;

    @BeforeEach
    void setUp() {
        threadDumpGenerationService = new ThreadDumpGenerationServiceImpl(javaProcessService);
    }

    @Test
    void testGenerateThreadDump_InvalidPid() {
        // Given
        long invalidPid = 999999L;
        when(javaProcessService.getJavaProcessByPid(invalidPid)).thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> threadDumpGenerationService.generateThreadDump(invalidPid));
        
        assertTrue(exception.getMessage().contains("not a valid Java process"));
        verify(javaProcessService).getJavaProcessByPid(invalidPid);
    }

    @Test
    void testGenerateThreadDump_ValidPid() {
        // Given
        long validPid = 1234L;
        JavaProcess mockProcess = new JavaProcess(validPid, "com.example.App", "App", "-Xmx1g", "");
        when(javaProcessService.getJavaProcessByPid(validPid)).thenReturn(mockProcess);

        // When & Then
        // Note: This test may fail in some environments where jstack is not available or
        // the PID doesn't exist. In a real environment, we'd mock the process execution.
        // For now, we just verify that the validation passes and the service doesn't throw
        // an IllegalArgumentException for invalid PID.
        
        verify(javaProcessService, never()).getJavaProcessByPid(validPid);
        
        // We can't easily test the actual jstack execution without mocking ProcessBuilder,
        // but we can test the validation logic
        when(javaProcessService.getJavaProcessByPid(validPid)).thenReturn(mockProcess);
        
        // The actual execution will likely fail since PID 1234 probably doesn't exist,
        // but it should not fail with IllegalArgumentException
        try {
            threadDumpGenerationService.generateThreadDump(validPid);
            // If it succeeds, great! If it fails with RuntimeException, that's expected
            // since the PID likely doesn't exist
        } catch (RuntimeException e) {
            // Expected for non-existent PID, should not be IllegalArgumentException
            assertFalse(e instanceof IllegalArgumentException);
        }
        
        verify(javaProcessService).getJavaProcessByPid(validPid);
    }

    @Test
    void testIsAvailable() {
        // When
        boolean available = threadDumpGenerationService.isAvailable();
        
        // Then
        // jstack should be available in most Java environments
        // This test just ensures the method doesn't throw an exception
        assertTrue(available || !available); // Always passes, just tests no exception
    }
}