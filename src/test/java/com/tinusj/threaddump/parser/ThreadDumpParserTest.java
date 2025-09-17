package com.tinusj.threaddump.parser;

import com.tinusj.threaddump.model.ThreadInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ThreadDumpParser.
 */
class ThreadDumpParserTest {
    
    private ThreadDumpParser parser;
    
    @BeforeEach
    void setUp() {
        parser = new ThreadDumpParser();
    }
    
    @Test
    void parse_ShouldReturnEmptyList_WhenGivenEmptyContent() {
        // Given
        String content = "";
        
        // When
        List<ThreadInfo> result = parser.parse(content);
        
        // Then
        assertThat(result).isEmpty();
    }
    
    @Test
    void parse_ShouldReturnEmptyList_WhenGivenNullContent() {
        // Given
        String content = null;
        
        // When
        List<ThreadInfo> result = parser.parse(content);
        
        // Then
        assertThat(result).isEmpty();
    }
    
    @Test
    void parse_ShouldParseBasicThreadInfo_WhenGivenValidThreadDump() {
        // Given
        String threadDump = "\"main\" #1 prio=5 os_prio=0 tid=0x00007f8c2c009000 nid=0x1234 runnable [0x00007f8c35a5e000]\n" +
                "   java.lang.Thread.State: RUNNABLE\n" +
                "   at java.lang.Object.wait(Native Method)\n" +
                "   at java.lang.Object.wait(Object.java:502)\n";
        
        // When
        List<ThreadInfo> result = parser.parse(threadDump);
        
        // Then
        assertThat(result).hasSize(1);
        ThreadInfo thread = result.get(0);
        assertThat(thread.name()).isEqualTo("main");
        assertThat(thread.id()).isEqualTo(1);
        assertThat(thread.priority()).isEqualTo(5);
    }
}