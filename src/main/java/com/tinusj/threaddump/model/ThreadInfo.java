package com.tinusj.threaddump.model;

import com.tinusj.threaddump.enums.ThreadState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

public record ThreadInfo(
        @NotNull Long threadId,
        @NotBlank String threadName,
        @NotNull ThreadState state,
        @PositiveOrZero Long cpuTime,
        @PositiveOrZero Long blockedTime,
        @PositiveOrZero Long waitedTime,
        String lockName,
        String lockOwnerName,
        Long lockOwnerId,
        List<StackTraceElement> stackTrace
) {}