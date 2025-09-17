package com.tinusj.threaddump.formatter;

import com.tinusj.threaddump.enums.ReportFormat;
import com.tinusj.threaddump.enums.ReportStatus;
import com.tinusj.threaddump.model.DiagnosticReport;
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
        DiagnosticReport report = new DiagnosticReport(
                "test-id",
                LocalDateTime.of(2023, 1, 1, 12, 0),
                "test-source",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                ReportStatus.COMPLETED,
                "Test summary"
        );
        
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