package com.tinusj.threaddump.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Main diagnostic report containing analysis results of a thread dump.
 */
@Data
@Builder
public class DiagnosticReport {
    
    private String id;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    private String source;
    private ThreadStatistics statistics;
    private List<DiagnosticFinding> findings;
    private List<String> suggestedFixes;
    
    @Builder.Default
    private String status = "COMPLETED";
    
    private String summary;
}