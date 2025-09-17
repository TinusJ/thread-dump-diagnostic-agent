package com.tinusj.threaddump.parser;

import com.tinusj.threaddump.model.ThreadInfo;
import com.tinusj.threaddump.enums.ThreadState;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser utility for extracting thread information from thread dump text.
 * This is a stub implementation that provides basic parsing functionality.
 */
@Component
public class ThreadDumpParser {
    
    private static final Pattern THREAD_HEADER_PATTERN = Pattern.compile(
        "\"([^\"]+)\"\\s*#(\\d+).*?prio=(\\d+).*?tid=([0-9a-fx]+).*?nid=([0-9a-fx]+)\\s+(\\w+)"
    );
    
    private static final Pattern THREAD_STATE_PATTERN = Pattern.compile(
        "java\\.lang\\.Thread\\.State:\\s*(\\w+)"
    );

    /**
     * Parses thread dump content and extracts thread information.
     * This is a basic stub implementation.
     * 
     * @param threadDumpContent the raw thread dump content
     * @return list of parsed thread information
     */
    public List<ThreadInfo> parse(String threadDumpContent) {
        List<ThreadInfo> threads = new ArrayList<>();
        
        if (threadDumpContent == null || threadDumpContent.trim().isEmpty()) {
            return threads;
        }
        
        // Split by thread boundaries (basic approach)
        String[] threadBlocks = threadDumpContent.split("(?=\"[^\"]*\"\\s*#\\d+)");
        
        for (String block : threadBlocks) {
            if (block.trim().isEmpty()) continue;
            
            ThreadInfo threadInfo = parseThreadBlock(block);
            if (threadInfo != null) {
                threads.add(threadInfo);
            }
        }
        
        return threads;
    }
    
    private ThreadInfo parseThreadBlock(String block) {
        String name = null;
        long id = 0;
        int priority = 0;
        ThreadState state = ThreadState.UNKNOWN;
        String lockName = null;
        String lockOwner = null;
        boolean daemon = false;
        String group = null;
        
        // Parse thread header
        Matcher headerMatcher = THREAD_HEADER_PATTERN.matcher(block);
        if (headerMatcher.find()) {
            name = headerMatcher.group(1);
            id = Long.parseLong(headerMatcher.group(2));
            priority = Integer.parseInt(headerMatcher.group(3));
        }
        
        // Parse thread state
        Matcher stateMatcher = THREAD_STATE_PATTERN.matcher(block);
        if (stateMatcher.find()) {
            try {
                state = ThreadState.valueOf(stateMatcher.group(1));
            } catch (IllegalArgumentException e) {
                state = ThreadState.UNKNOWN;
            }
        }
        
        // Extract stack trace (simplified)
        List<String> stackTrace = extractStackTrace(block);
        
        // Check if daemon thread
        daemon = block.contains("daemon");
        
        return new ThreadInfo(name, id, state, lockName, lockOwner, stackTrace, daemon, priority, group);
    }
    
    private List<String> extractStackTrace(String block) {
        List<String> stackTrace = new ArrayList<>();
        String[] lines = block.split("\n");
        
        boolean inStackTrace = false;
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("at ")) {
                stackTrace.add(line);
                inStackTrace = true;
            } else if (inStackTrace && line.isEmpty()) {
                break;
            }
        }
        
        return stackTrace;
    }
}