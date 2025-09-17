package com.tinusj.threaddump.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Represents information about a single thread in a thread dump.
 */
@Data
@Builder
public class ThreadInfo {
    private String name;
    private long id;
    private ThreadState state;
    private String lockName;
    private String lockOwner;
    private List<String> stackTrace;
    private boolean daemon;
    private int priority;
    private String group;
}