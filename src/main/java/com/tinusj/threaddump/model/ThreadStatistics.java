package com.tinusj.threaddump.model;

import com.tinusj.threaddump.enums.ThreadState;

import java.util.Map;

/**
 * Contains statistical information about threads in a thread dump.
 */
public record ThreadStatistics(
    int totalThreads,
    Map<ThreadState, Integer> threadsByState,
    int daemonThreads,
    int blockedThreads,
    int waitingThreads,
    int runnableThreads,
    Map<String, Integer> threadGroups
) {}