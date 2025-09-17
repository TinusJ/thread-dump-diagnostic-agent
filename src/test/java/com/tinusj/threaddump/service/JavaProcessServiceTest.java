package com.tinusj.threaddump.service;

import com.tinusj.threaddump.model.JavaProcess;
import com.tinusj.threaddump.service.impl.JavaProcessServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for JavaProcessService implementation.
 */
class JavaProcessServiceTest {

    private JavaProcessService javaProcessService;

    @BeforeEach
    void setUp() {
        javaProcessService = new JavaProcessServiceImpl();
    }

    @Test
    void testGetRunningJavaProcesses() {
        // When
        List<JavaProcess> processes = javaProcessService.getRunningJavaProcesses();

        // Then
        assertNotNull(processes);
        // Note: We can't guarantee specific processes are running in the test environment,
        // but we can verify that the method returns a valid list without throwing exceptions
    }

    @Test
    void testGetJavaProcessByPid() {
        // Given
        List<JavaProcess> processes = javaProcessService.getRunningJavaProcesses();
        
        if (!processes.isEmpty()) {
            JavaProcess firstProcess = processes.get(0);
            
            // When
            JavaProcess foundProcess = javaProcessService.getJavaProcessByPid(firstProcess.pid());
            
            // Then
            assertNotNull(foundProcess);
            assertEquals(firstProcess.pid(), foundProcess.pid());
        }
        
        // Test with non-existent PID
        JavaProcess nonExistentProcess = javaProcessService.getJavaProcessByPid(999999L);
        assertNull(nonExistentProcess);
    }
}