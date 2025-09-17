package com.tinusj.threaddump.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Contains statistical information about threads in a thread dump.
 */
@Data
@Builder
public class ThreadStatistics {
    private int totalThreads;
    private Map<ThreadState, Integer> threadsByState;
    private int daemonThreads;
    private int blockedThreads;
    private int waitingThreads;
    private int runnableThreads;
}