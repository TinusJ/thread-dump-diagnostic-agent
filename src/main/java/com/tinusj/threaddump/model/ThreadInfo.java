package com.tinusj.threaddump.model;

import com.tinusj.threaddump.enums.ThreadState;

import java.util.List;

/**
 * Represents information about a single thread in a thread dump.
 */
public record ThreadInfo(
    String name,
    long id,
    ThreadState state,
    String lockName,
    String lockOwner,
    List<String> stackTrace,
    boolean daemon,
    int priority,
    String group
) {}