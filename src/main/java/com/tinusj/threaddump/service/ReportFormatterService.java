package com.tinusj.threaddump.service;

import com.tinusj.threaddump.formatter.ReportFormatter;
import com.tinusj.threaddump.model.DiagnosticReport;
import com.tinusj.threaddump.model.ReportFormat;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service for managing different report formatters.
 */
@Service
public class ReportFormatterService {
    
    private final Map<ReportFormat, ReportFormatter> formatters;
    
    public ReportFormatterService(List<ReportFormatter> formatterList) {
        this.formatters = formatterList.stream()
                .collect(Collectors.toMap(ReportFormatter::getFormat, Function.identity()));
    }
    
    /**
     * Formats a diagnostic report in the specified format.
     * 
     * @param report the diagnostic report to format
     * @param format the desired output format
     * @return formatted report as string
     * @throws IllegalArgumentException if the format is not supported
     */
    public String formatReport(DiagnosticReport report, ReportFormat format) {
        ReportFormatter formatter = formatters.get(format);
        if (formatter == null) {
            throw new IllegalArgumentException("Unsupported format: " + format);
        }
        return formatter.format(report);
    }
    
    /**
     * Returns all supported report formats.
     * 
     * @return set of supported formats
     */
    public java.util.Set<ReportFormat> getSupportedFormats() {
        return formatters.keySet();
    }
}