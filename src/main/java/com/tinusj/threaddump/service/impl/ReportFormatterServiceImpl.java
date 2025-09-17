package com.tinusj.threaddump.service.impl;

import com.tinusj.threaddump.enums.ReportFormat;
import com.tinusj.threaddump.formatter.ReportFormatter;
import com.tinusj.threaddump.model.DiagnosticReport;
import com.tinusj.threaddump.service.ReportFormatterService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementation of ReportFormatterService for managing different report formatters.
 */
@Service
public class ReportFormatterServiceImpl implements ReportFormatterService {
    
    private final Map<ReportFormat, ReportFormatter> formatters;
    
    public ReportFormatterServiceImpl(List<ReportFormatter> formatterList) {
        this.formatters = formatterList.stream()
                .collect(Collectors.toMap(ReportFormatter::getFormat, Function.identity()));
    }
    
    @Override
    public String formatReport(DiagnosticReport report, ReportFormat format) {
        ReportFormatter formatter = formatters.get(format);
        if (formatter == null) {
            throw new IllegalArgumentException("Unsupported format: " + format);
        }
        return formatter.format(report);
    }
    
    @Override
    public Set<ReportFormat> getSupportedFormats() {
        return formatters.keySet();
    }
}