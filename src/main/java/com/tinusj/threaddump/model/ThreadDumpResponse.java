package com.tinusj.threaddump.model;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record ThreadDumpResponse(
        @NotNull String dumpId,
        @NotNull LocalDateTime timestamp,
        @NotNull String processId,
        int totalThreads,
        List<ThreadInfo> threads,
        List<DiagnosticReport> diagnostics
) {}