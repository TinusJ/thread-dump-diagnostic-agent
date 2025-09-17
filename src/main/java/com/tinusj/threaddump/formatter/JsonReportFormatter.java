package com.tinusj.threaddump.formatter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tinusj.threaddump.model.DiagnosticReport;
import com.tinusj.threaddump.model.ReportFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Formats diagnostic reports as JSON.
 */
@Component
@Slf4j
public class JsonReportFormatter implements ReportFormatter {
    
    private final ObjectMapper objectMapper;
    
    public JsonReportFormatter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
    
    @Override
    public String format(DiagnosticReport report) {
        try {
            return objectMapper.writeValueAsString(report);
        } catch (Exception e) {
            log.error("Error formatting report as JSON", e);
            return "{\"error\": \"Failed to format report as JSON: " + e.getMessage() + "\"}";
        }
    }
    
    @Override
    public ReportFormat getFormat() {
        return ReportFormat.JSON;
    }
}