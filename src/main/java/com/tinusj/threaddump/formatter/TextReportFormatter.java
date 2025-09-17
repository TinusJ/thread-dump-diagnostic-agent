package com.tinusj.threaddump.formatter;

import com.tinusj.threaddump.model.DiagnosticFinding;
import com.tinusj.threaddump.model.DiagnosticReport;
import com.tinusj.threaddump.model.ReportFormat;
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
            sb.append("Report ID: ").append(report.getId()).append("\n");
            sb.append("Timestamp: ").append(report.getTimestamp().format(DATE_FORMATTER)).append("\n");
            sb.append("Source: ").append(report.getSource()).append("\n");
            sb.append("Status: ").append(report.getStatus()).append("\n\n");
            
            // Summary
            sb.append("SUMMARY\n");
            sb.append("-------\n");
            sb.append(report.getSummary()).append("\n\n");
            
            // Statistics
            if (report.getStatistics() != null) {
                ThreadStatistics stats = report.getStatistics();
                sb.append("THREAD STATISTICS\n");
                sb.append("-----------------\n");
                sb.append("Total Threads: ").append(stats.getTotalThreads()).append("\n");
                sb.append("Daemon Threads: ").append(stats.getDaemonThreads()).append("\n");
                sb.append("Runnable Threads: ").append(stats.getRunnableThreads()).append("\n");
                sb.append("Blocked Threads: ").append(stats.getBlockedThreads()).append("\n");
                sb.append("Waiting Threads: ").append(stats.getWaitingThreads()).append("\n\n");
                
                if (stats.getThreadsByState() != null && !stats.getThreadsByState().isEmpty()) {
                    sb.append("Threads by State:\n");
                    stats.getThreadsByState().forEach((state, count) ->
                            sb.append("  ").append(state).append(": ").append(count).append("\n"));
                    sb.append("\n");
                }
            }
            
            // Findings
            if (report.getFindings() != null && !report.getFindings().isEmpty()) {
                sb.append("DIAGNOSTIC FINDINGS\n");
                sb.append("-------------------\n");
                
                for (int i = 0; i < report.getFindings().size(); i++) {
                    DiagnosticFinding finding = report.getFindings().get(i);
                    sb.append(String.format("%d. %s [%s]\n", i + 1, finding.getType(), finding.getSeverity()));
                    sb.append("   Description: ").append(finding.getDescription()).append("\n");
                    
                    if (finding.getAffectedThreads() != null && !finding.getAffectedThreads().isEmpty()) {
                        sb.append("   Affected Threads: ").append(String.join(", ", finding.getAffectedThreads())).append("\n");
                    }
                    
                    if (finding.getRecommendation() != null) {
                        sb.append("   Recommendation: ").append(finding.getRecommendation()).append("\n");
                    }
                    
                    sb.append("\n");
                }
            }
            
            // Suggested fixes
            if (report.getSuggestedFixes() != null && !report.getSuggestedFixes().isEmpty()) {
                sb.append("SUGGESTED FIXES\n");
                sb.append("---------------\n");
                
                for (int i = 0; i < report.getSuggestedFixes().size(); i++) {
                    sb.append(String.format("%d. %s\n", i + 1, report.getSuggestedFixes().get(i)));
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