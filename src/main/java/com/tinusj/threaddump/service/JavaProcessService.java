package com.tinusj.threaddump.service;

import com.tinusj.threaddump.model.JavaProcess;

import java.util.List;

/**
 * Service for detecting and managing Java processes.
 */
public interface JavaProcessService {
    
    /**
     * Gets a list of all running Java processes.
     * 
     * @return list of Java processes with their PIDs and information
     */
    List<JavaProcess> getRunningJavaProcesses();
    
    /**
     * Gets information about a specific Java process by PID.
     * 
     * @param pid the process ID
     * @return Java process information or null if not found or not a Java process
     */
    JavaProcess getJavaProcessByPid(long pid);
}