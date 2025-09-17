package com.tinusj.threaddump.service;

/**
 * Service for generating thread dumps from running Java processes.
 */
public interface ThreadDumpGenerationService {
    
    /**
     * Generates a thread dump for the specified process ID.
     * 
     * @param pid the process ID of the Java process
     * @return the thread dump content as a string
     * @throws IllegalArgumentException if the PID is invalid or not a Java process
     * @throws RuntimeException if thread dump generation fails
     */
    String generateThreadDump(long pid);
    
    /**
     * Checks if thread dump generation is available on this system.
     * 
     * @return true if thread dump generation tools are available, false otherwise
     */
    boolean isAvailable();
}