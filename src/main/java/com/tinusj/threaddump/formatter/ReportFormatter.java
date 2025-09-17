package com.tinusj.threaddump.formatter;

import com.tinusj.threaddump.model.DiagnosticReport;
import com.tinusj.threaddump.model.ReportFormat;

/**
 * Interface for formatting diagnostic reports in different output formats.
 */
public interface ReportFormatter {
    
    /**
     * Formats the diagnostic report according to the implementation's format.
     * 
     * @param report the diagnostic report to format
     * @return formatted report as string
     */
    String format(DiagnosticReport report);
    
    /**
     * Returns the format type this formatter handles.
     * 
     * @return the report format
     */
    ReportFormat getFormat();
}