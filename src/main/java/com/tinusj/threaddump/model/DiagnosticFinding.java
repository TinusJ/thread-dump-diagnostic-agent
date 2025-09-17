package com.tinusj.threaddump.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Represents a diagnostic finding from thread dump analysis.
 */
@Data
@Builder
public class DiagnosticFinding {
    
    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    private String type;
    private String description;
    private Severity severity;
    private List<String> affectedThreads;
    private String recommendation;
    private Object details;
}