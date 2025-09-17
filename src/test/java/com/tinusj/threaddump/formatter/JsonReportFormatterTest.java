package com.tinusj.threaddump.formatter;

import com.tinusj.threaddump.model.DiagnosticReport;
import com.tinusj.threaddump.model.ReportFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for JsonReportFormatter.
 */
class JsonReportFormatterTest {
    
    private JsonReportFormatter formatter;
    
    @BeforeEach
    void setUp() {
        formatter = new JsonReportFormatter();
    }
    
    @Test
    void format_ShouldReturnValidJson_WhenGivenValidReport() {
        // Given
        DiagnosticReport report = DiagnosticReport.builder()
                .id("test-id")
                .timestamp(LocalDateTime.of(2023, 1, 1, 12, 0))
                .source("test-source")
                .status("COMPLETED")
                .summary("Test summary")
                .findings(new ArrayList<>())
                .suggestedFixes(new ArrayList<>())
                .build();
        
        // When
        String result = formatter.format(report);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).contains("\"id\" : \"test-id\"");
        assertThat(result).contains("\"source\" : \"test-source\"");
        assertThat(result).contains("\"status\" : \"COMPLETED\"");
    }
    
    @Test
    void getFormat_ShouldReturnJson() {
        // When
        ReportFormat format = formatter.getFormat();
        
        // Then
        assertThat(format).isEqualTo(ReportFormat.JSON);
    }
}