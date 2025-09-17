package com.tinusj.threaddump.service;

import com.tinusj.threaddump.model.DiagnosticReport;

/**
 * Interface for thread dump diagnostic analysis services.
 */
public interface DiagnosticService {
    
    /**
     * Analyzes thread dump content and generates a comprehensive diagnostic report.
     * 
     * @param threadDumpContent the raw thread dump content
     * @param source the source identifier (e.g., filename, "text-input")
     * @return diagnostic report with analysis results
     */
    DiagnosticReport analyzeThreadDump(String threadDumpContent, String source);
}