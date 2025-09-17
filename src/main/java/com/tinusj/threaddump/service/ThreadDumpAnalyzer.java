package com.tinusj.threaddump.service;

import com.tinusj.threaddump.model.DiagnosticFinding;
import com.tinusj.threaddump.model.ThreadStatistics;

import java.util.List;

/**
 * Interface for analyzing thread dumps and generating diagnostic findings.
 */
public interface ThreadDumpAnalyzer {
    
    /**
     * Analyzes thread dump content and generates statistics.
     * 
     * @param threadDumpContent the raw thread dump content
     * @return thread statistics
     */
    ThreadStatistics analyzeStatistics(String threadDumpContent);
    
    /**
     * Analyzes thread dump content and generates diagnostic findings.
     * 
     * @param threadDumpContent the raw thread dump content
     * @return list of diagnostic findings
     */
    List<DiagnosticFinding> analyzeFindings(String threadDumpContent);
}