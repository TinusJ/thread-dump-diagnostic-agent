package com.tinusj.threaddump.skill;

import com.tinusj.threaddump.model.DiagnosticReport;
import com.tinusj.threaddump.model.ReportFormat;
import com.tinusj.threaddump.service.DiagnosticService;
import com.tinusj.threaddump.service.ReportFormatterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * MCP skill for analyzing Java thread dumps.
 * Note: This is a placeholder implementation for MCP functionality.
 * When Spring AI MCP Server is available, this can be enhanced with proper MCP annotations.
 */
@Component
@Slf4j
public class ThreadDumpAnalysisSkill {
    
    private final DiagnosticService diagnosticService;
    private final ReportFormatterService reportFormatterService;
    
    public ThreadDumpAnalysisSkill(DiagnosticService diagnosticService, 
                                 ReportFormatterService reportFormatterService) {
        this.diagnosticService = diagnosticService;
        this.reportFormatterService = reportFormatterService;
    }
    
    /**
     * Handles the thread dump analysis tool invocation.
     * This method provides the core functionality that will be exposed as an MCP tool.
     * 
     * Expected arguments:
     * - content (required): The thread dump content to analyze
     * - format (optional): Output format (JSON, XML, TEXT) - defaults to JSON
     * - source (optional): Source identifier - defaults to "mcp-input"
     */
    public String handleAnalyzeThreadDump(Map<String, Object> arguments) {
        try {
            String content = (String) arguments.get("content");
            String formatStr = (String) arguments.getOrDefault("format", "JSON");
            String source = (String) arguments.getOrDefault("source", "mcp-input");
            
            if (content == null || content.trim().isEmpty()) {
                return "Error: Thread dump content cannot be empty";
            }
            
            ReportFormat format;
            try {
                format = ReportFormat.valueOf(formatStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return "Error: Unsupported format '" + formatStr + "'. Supported formats: JSON, XML, TEXT";
            }
            
            log.info("MCP: Analyzing thread dump from source: {}, format: {}", source, format);
            
            DiagnosticReport report = diagnosticService.analyzThreadDump(content, source);
            String formattedReport = reportFormatterService.formatReport(report, format);
            
            log.info("MCP: Thread dump analysis completed successfully, report ID: {}", report.getId());
            
            return formattedReport;
            
        } catch (Exception e) {
            log.error("MCP: Error analyzing thread dump", e);
            return "Error: Failed to analyze thread dump - " + e.getMessage();
        }
    }
    
    /**
     * Returns the MCP tool definition that would be used when Spring AI MCP Server is available.
     * This is provided for documentation and future integration purposes.
     */
    public String getMcpToolDefinition() {
        return "{\n" +
                "  \"name\": \"analyze_thread_dump\",\n" +
                "  \"description\": \"Analyzes Java thread dump content and provides diagnostic insights including thread statistics, deadlock detection, and performance recommendations\",\n" +
                "  \"inputSchema\": {\n" +
                "    \"type\": \"object\",\n" +
                "    \"properties\": {\n" +
                "      \"content\": {\n" +
                "        \"type\": \"string\",\n" +
                "        \"description\": \"The thread dump content to analyze\"\n" +
                "      },\n" +
                "      \"format\": {\n" +
                "        \"type\": \"string\",\n" +
                "        \"enum\": [\"JSON\", \"XML\", \"TEXT\"],\n" +
                "        \"description\": \"Output format for the diagnostic report\",\n" +
                "        \"default\": \"JSON\"\n" +
                "      },\n" +
                "      \"source\": {\n" +
                "        \"type\": \"string\",\n" +
                "        \"description\": \"Source identifier for the thread dump\",\n" +
                "        \"default\": \"mcp-input\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"required\": [\"content\"]\n" +
                "  }\n" +
                "}";
    }
}