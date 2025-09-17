package com.tinusj.threaddump.model;

/**
 * Enumeration representing thread states found in thread dumps.
 */
public enum ThreadState {
    NEW,
    RUNNABLE,
    BLOCKED,
    WAITING,
    TIMED_WAITING,
    TERMINATED,
    UNKNOWN
}