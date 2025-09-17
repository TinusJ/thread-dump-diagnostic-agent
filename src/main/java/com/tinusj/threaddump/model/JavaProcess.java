package com.tinusj.threaddump.model;

/**
 * Represents information about a running Java process.
 */
public record JavaProcess(
    long pid,
    String mainClass,
    String displayName,
    String jvmArguments,
    String applicationArguments
) {}