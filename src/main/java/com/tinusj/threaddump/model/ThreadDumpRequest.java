package com.tinusj.threaddump.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record ThreadDumpRequest(
        @NotBlank String processId,
        @PositiveOrZero Integer timeoutSeconds,
        boolean includeStackTraces,
        boolean analyzePerformance
) {}