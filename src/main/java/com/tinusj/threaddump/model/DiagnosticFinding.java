package com.tinusj.threaddump.model;

import com.tinusj.threaddump.enums.Severity;

import java.util.List;

/**
 * Represents a diagnostic finding from thread dump analysis.
 */
public record DiagnosticFinding(
    String type,
    String description,
    Severity severity,
    List<String> affectedThreads,
    String recommendation,
    Object details
) {}