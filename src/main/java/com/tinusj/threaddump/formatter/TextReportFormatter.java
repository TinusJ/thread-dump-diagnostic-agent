package com.tinusj.threaddump.formatter;

import com.tinusj.threaddump.model.DiagnosticFinding;
import com.tinusj.threaddump.model.DiagnosticReport;
import com.tinusj.threaddump.enums.ReportFormat;
import com.tinusj.threaddump.model.ThreadStatistics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * Formats diagnostic reports as plain text.
 */
@Component
@Slf4j
public class TextReportFormatter implements ReportFormatter {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public String format(DiagnosticReport report) {
        try {
            StringBuilder sb = new StringBuilder();
            
            // Header
            sb.append("THREAD DUMP DIAGNOSTIC REPORT\n");
            sb.append("============================\n\n");
            
            // Basic info
            sb.append("Report ID: ").append(report.id()).append("\n");
            sb.append("Timestamp: ").append(report.timestamp().format(DATE_FORMATTER)).append("\n");
            sb.append("Source: ").append(report.source()).append("\n");
            sb.append("Status: ").append(report.status()).append("\n\n");
            
            // Summary
            sb.append("SUMMARY\n");
            sb.append("-------\n");
            sb.append(report.summary()).append("\n\n");
            
            // Statistics
            if (report.statistics() != null) {
                ThreadStatistics stats = report.statistics();
                sb.append("THREAD STATISTICS\n");
                sb.append("-----------------\n");
                sb.append("Total Threads: ").append(stats.totalThreads()).append("\n");
                sb.append("Daemon Threads: ").append(stats.daemonThreads()).append("\n");
                sb.append("Runnable Threads: ").append(stats.runnableThreads()).append("\n");
                sb.append("Blocked Threads: ").append(stats.blockedThreads()).append("\n");
                sb.append("Waiting Threads: ").append(stats.waitingThreads()).append("\n\n");
                
                if (stats.threadsByState() != null && !stats.threadsByState().isEmpty()) {
                    sb.append("Threads by State:\n");
                    stats.threadsByState().forEach((state, count) ->
                            sb.append("  ").append(state).append(": ").append(count).append("\n"));
                    sb.append("\n");
                }
            }
            
            // Findings
            if (report.findings() != null && !report.findings().isEmpty()) {
                sb.append("DIAGNOSTIC FINDINGS\n");
                sb.append("-------------------\n");
                
                for (int i = 0; i < report.findings().size(); i++) {
                    DiagnosticFinding finding = report.findings().get(i);
                    sb.append(String.format("%d. %s [%s]\n", i + 1, finding.type(), finding.severity()));
                    sb.append("   Description: ").append(finding.description()).append("\n");
                    
                    if (finding.affectedThreads() != null && !finding.affectedThreads().isEmpty()) {
                        sb.append("   Affected Threads: ").append(String.join(", ", finding.affectedThreads())).append("\n");
                    }
                    
                    if (finding.recommendation() != null) {
                        sb.append("   Recommendation: ").append(finding.recommendation()).append("\n");
                    }
                    
                    sb.append("\n");
                }
            }
            
            // Suggested fixes
            if (report.suggestedFixes() != null && !report.suggestedFixes().isEmpty()) {
                sb.append("SUGGESTED FIXES\n");
                sb.append("---------------\n");
                
                for (int i = 0; i < report.suggestedFixes().size(); i++) {
                    sb.append(String.format("%d. %s\n", i + 1, report.suggestedFixes().get(i)));
                }
                sb.append("\n");
            }
            
            // Footer
            sb.append("End of Report\n");
            
            return sb.toString();
            
        } catch (Exception e) {
            log.error("Error formatting report as text", e);
            return "Error: Failed to format report as text: " + e.getMessage();
        }
    }
    
    @Override
    public ReportFormat getFormat() {
        return ReportFormat.TEXT;
    }
}