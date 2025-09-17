package com.tinusj.threaddump.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tinusj.threaddump.enums.ReportStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Main diagnostic report containing analysis results of a thread dump.
 */
public record DiagnosticReport(
    String id,
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime timestamp,
    
    String source,
    ThreadStatistics statistics,
    List<DiagnosticFinding> findings,
    List<String> suggestedFixes,
    ReportStatus status,
    String summary
) {}