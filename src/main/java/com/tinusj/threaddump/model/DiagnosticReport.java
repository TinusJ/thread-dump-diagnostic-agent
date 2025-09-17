package com.tinusj.threaddump.model;

import com.tinusj.threaddump.enums.DiagnosticSeverity;
import com.tinusj.threaddump.enums.DiagnosticType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record DiagnosticReport(
        @NotBlank String reportId,
        @NotNull LocalDateTime timestamp,
        @NotNull DiagnosticType type,
        @NotNull DiagnosticSeverity severity,
        @NotBlank String description,
        List<ThreadInfo> affectedThreads,
        Map<String, Object> metrics,
        List<String> recommendations
) {}