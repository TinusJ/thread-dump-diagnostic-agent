package com.tinusj.threaddump.service;

import com.tinusj.threaddump.enums.ReportFormat;
import com.tinusj.threaddump.model.DiagnosticReport;

import java.util.Set;

/**
 * Interface for managing different report formatters.
 */
public interface ReportFormatterService {
    
    /**
     * Formats a diagnostic report in the specified format.
     * 
     * @param report the diagnostic report to format
     * @param format the desired output format
     * @return formatted report as string
     * @throws IllegalArgumentException if the format is not supported
     */
    String formatReport(DiagnosticReport report, ReportFormat format);
    
    /**
     * Returns all supported report formats.
     * 
     * @return set of supported formats
     */
    Set<ReportFormat> getSupportedFormats();
}