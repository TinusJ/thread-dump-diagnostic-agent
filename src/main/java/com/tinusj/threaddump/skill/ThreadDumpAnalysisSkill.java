package com.tinusj.threaddump.skill;

import com.tinusj.threaddump.model.DiagnosticReport;
import com.tinusj.threaddump.model.JavaProcess;
import com.tinusj.threaddump.enums.ReportFormat;
import com.tinusj.threaddump.service.DiagnosticService;
import com.tinusj.threaddump.service.JavaProcessService;
import com.tinusj.threaddump.service.ReportFormatterService;
import com.tinusj.threaddump.service.ThreadDumpGenerationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
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
    private final JavaProcessService javaProcessService;
    private final ThreadDumpGenerationService threadDumpGenerationService;
    
    public ThreadDumpAnalysisSkill(DiagnosticService diagnosticService, 
                                 ReportFormatterService reportFormatterService,
                                 JavaProcessService javaProcessService,
                                 ThreadDumpGenerationService threadDumpGenerationService) {
        this.diagnosticService = diagnosticService;
        this.reportFormatterService = reportFormatterService;
        this.javaProcessService = javaProcessService;
        this.threadDumpGenerationService = threadDumpGenerationService;
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
            
            DiagnosticReport report = diagnosticService.analyzeThreadDump(content, source);
            String formattedReport = reportFormatterService.formatReport(report, format);
            
            log.info("MCP: Thread dump analysis completed successfully, report ID: {}", report.id());
            
            return formattedReport;
            
        } catch (Exception e) {
            log.error("MCP: Error analyzing thread dump", e);
            return "Error: Failed to analyze thread dump - " + e.getMessage();
        }
    }
    
    /**
     * Handles the Java process detection tool invocation.
     * This method provides functionality to get all running Java processes.
     * 
     * Expected arguments:
     * - format (optional): Output format (JSON, XML, TEXT) - defaults to JSON
     */
    public String handleGetJavaProcesses(Map<String, Object> arguments) {
        try {
            String formatStr = (String) arguments.getOrDefault("format", "JSON");
            
            ReportFormat format;
            try {
                format = ReportFormat.valueOf(formatStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return "Error: Unsupported format '" + formatStr + "'. Supported formats: JSON, XML, TEXT";
            }
            
            log.info("MCP: Getting running Java processes, format: {}", format);
            
            List<JavaProcess> processes = javaProcessService.getRunningJavaProcesses();
            
            log.info("MCP: Found {} Java processes", processes.size());
            
            // Format the output based on requested format
            String result = formatJavaProcesses(processes, format);
            
            return result;
            
        } catch (Exception e) {
            log.error("MCP: Error getting Java processes", e);
            return "Error: Failed to get Java processes - " + e.getMessage();
        }
    }
    
    /**
     * Formats the list of Java processes according to the specified format.
     */
    private String formatJavaProcesses(List<JavaProcess> processes, ReportFormat format) {
        switch (format) {
            case JSON:
                return formatProcessesAsJson(processes);
            case XML:
                return formatProcessesAsXml(processes);
            case TEXT:
                return formatProcessesAsText(processes);
            default:
                return formatProcessesAsJson(processes);
        }
    }
    
    private String formatProcessesAsJson(List<JavaProcess> processes) {
        StringBuilder json = new StringBuilder();
        json.append("{\n  \"javaProcesses\": [\n");
        
        for (int i = 0; i < processes.size(); i++) {
            JavaProcess process = processes.get(i);
            json.append("    {\n");
            json.append("      \"pid\": ").append(process.pid()).append(",\n");
            json.append("      \"mainClass\": \"").append(escapeJson(process.mainClass())).append("\",\n");
            json.append("      \"displayName\": \"").append(escapeJson(process.displayName())).append("\",\n");
            json.append("      \"jvmArguments\": \"").append(escapeJson(process.jvmArguments())).append("\",\n");
            json.append("      \"applicationArguments\": \"").append(escapeJson(process.applicationArguments())).append("\"\n");
            json.append("    }");
            
            if (i < processes.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        
        json.append("  ],\n");
        json.append("  \"count\": ").append(processes.size()).append("\n");
        json.append("}");
        
        return json.toString();
    }
    
    private String formatProcessesAsXml(List<JavaProcess> processes) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<javaProcesses count=\"").append(processes.size()).append("\">\n");
        
        for (JavaProcess process : processes) {
            xml.append("  <process>\n");
            xml.append("    <pid>").append(process.pid()).append("</pid>\n");
            xml.append("    <mainClass>").append(escapeXml(process.mainClass())).append("</mainClass>\n");
            xml.append("    <displayName>").append(escapeXml(process.displayName())).append("</displayName>\n");
            xml.append("    <jvmArguments>").append(escapeXml(process.jvmArguments())).append("</jvmArguments>\n");
            xml.append("    <applicationArguments>").append(escapeXml(process.applicationArguments())).append("</applicationArguments>\n");
            xml.append("  </process>\n");
        }
        
        xml.append("</javaProcesses>");
        
        return xml.toString();
    }
    
    private String formatProcessesAsText(List<JavaProcess> processes) {
        StringBuilder text = new StringBuilder();
        text.append("Running Java Processes (").append(processes.size()).append(" found):\n");
        text.append("=====================================\n\n");
        
        for (JavaProcess process : processes) {
            text.append("PID: ").append(process.pid()).append("\n");
            text.append("Main Class: ").append(process.mainClass()).append("\n");
            text.append("Display Name: ").append(process.displayName()).append("\n");
            
            if (!process.jvmArguments().isEmpty()) {
                text.append("JVM Arguments: ").append(process.jvmArguments()).append("\n");
            }
            
            if (!process.applicationArguments().isEmpty()) {
                text.append("Application Arguments: ").append(process.applicationArguments()).append("\n");
            }
            
            text.append("\n");
        }
        
        return text.toString();
    }
    
    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    
    private String escapeXml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
    
    /**
     * Returns the MCP tool definitions that would be used when Spring AI MCP Server is available.
     * This is provided for documentation and future integration purposes.
     */
    public String getMcpToolDefinition() {
        return "{\n" +
                "  \"tools\": [\n" +
                "    {\n" +
                "      \"name\": \"analyze_thread_dump\",\n" +
                "      \"description\": \"Analyzes Java thread dump content and provides diagnostic insights including thread statistics, deadlock detection, and performance recommendations\",\n" +
                "      \"inputSchema\": {\n" +
                "        \"type\": \"object\",\n" +
                "        \"properties\": {\n" +
                "          \"content\": {\n" +
                "            \"type\": \"string\",\n" +
                "            \"description\": \"The thread dump content to analyze\"\n" +
                "          },\n" +
                "          \"format\": {\n" +
                "            \"type\": \"string\",\n" +
                "            \"enum\": [\"JSON\", \"XML\", \"TEXT\"],\n" +
                "            \"description\": \"Output format for the diagnostic report\",\n" +
                "            \"default\": \"JSON\"\n" +
                "          },\n" +
                "          \"source\": {\n" +
                "            \"type\": \"string\",\n" +
                "            \"description\": \"Source identifier for the thread dump\",\n" +
                "            \"default\": \"mcp-input\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"required\": [\"content\"]\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"get_java_processes\",\n" +
                "      \"description\": \"Gets information about all running Java processes including their PIDs, main classes, and arguments\",\n" +
                "      \"inputSchema\": {\n" +
                "        \"type\": \"object\",\n" +
                "        \"properties\": {\n" +
                "          \"format\": {\n" +
                "            \"type\": \"string\",\n" +
                "            \"enum\": [\"JSON\", \"XML\", \"TEXT\"],\n" +
                "            \"description\": \"Output format for the process list\",\n" +
                "            \"default\": \"JSON\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"required\": []\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"generate_thread_dump\",\n" +
                "      \"description\": \"Generates a thread dump from a running Java process using its PID\",\n" +
                "      \"inputSchema\": {\n" +
                "        \"type\": \"object\",\n" +
                "        \"properties\": {\n" +
                "          \"pid\": {\n" +
                "            \"type\": \"integer\",\n" +
                "            \"description\": \"The process ID of the Java process to generate thread dump for\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"required\": [\"pid\"]\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"generate_and_analyze_thread_dump\",\n" +
                "      \"description\": \"Generates a thread dump from a running Java process and immediately analyzes it for diagnostic insights\",\n" +
                "      \"inputSchema\": {\n" +
                "        \"type\": \"object\",\n" +
                "        \"properties\": {\n" +
                "          \"pid\": {\n" +
                "            \"type\": \"integer\",\n" +
                "            \"description\": \"The process ID of the Java process to generate thread dump for\"\n" +
                "          },\n" +
                "          \"format\": {\n" +
                "            \"type\": \"string\",\n" +
                "            \"enum\": [\"JSON\", \"XML\", \"TEXT\"],\n" +
                "            \"description\": \"Output format for the diagnostic report\",\n" +
                "            \"default\": \"JSON\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"required\": [\"pid\"]\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }
    
    /**
     * Handles the thread dump generation tool invocation.
     * This method provides the core functionality for generating thread dumps from PIDs.
     * 
     * Expected arguments:
     * - pid (required): The process ID to generate thread dump for
     */
    public String handleGenerateThreadDump(Map<String, Object> arguments) {
        try {
            if (!threadDumpGenerationService.isAvailable()) {
                return "Error: Thread dump generation is not available on this system";
            }
            
            Object pidObj = arguments.get("pid");
            if (pidObj == null) {
                return "Error: PID is required";
            }
            
            long pid;
            if (pidObj instanceof Number) {
                pid = ((Number) pidObj).longValue();
            } else if (pidObj instanceof String) {
                try {
                    pid = Long.parseLong((String) pidObj);
                } catch (NumberFormatException e) {
                    return "Error: Invalid PID format - must be a number";
                }
            } else {
                return "Error: Invalid PID type - must be a number or string";
            }
            
            log.info("MCP: Generating thread dump for PID: {}", pid);
            
            String threadDump = threadDumpGenerationService.generateThreadDump(pid);
            
            log.info("MCP: Thread dump generation completed successfully for PID: {}", pid);
            
            return threadDump;
            
        } catch (IllegalArgumentException e) {
            log.error("MCP: Invalid PID for thread dump generation", e);
            return "Error: " + e.getMessage();
        } catch (Exception e) {
            log.error("MCP: Error generating thread dump", e);
            return "Error: Failed to generate thread dump - " + e.getMessage();
        }
    }
    
    /**
     * Handles the thread dump generation and analysis tool invocation.
     * This method provides the core functionality for generating and analyzing thread dumps from PIDs.
     * 
     * Expected arguments:
     * - pid (required): The process ID to generate thread dump for
     * - format (optional): Output format (JSON, XML, TEXT) - defaults to JSON
     */
    public String handleGenerateAndAnalyzeThreadDump(Map<String, Object> arguments) {
        try {
            if (!threadDumpGenerationService.isAvailable()) {
                return "Error: Thread dump generation is not available on this system";
            }
            
            Object pidObj = arguments.get("pid");
            if (pidObj == null) {
                return "Error: PID is required";
            }
            
            long pid;
            if (pidObj instanceof Number) {
                pid = ((Number) pidObj).longValue();
            } else if (pidObj instanceof String) {
                try {
                    pid = Long.parseLong((String) pidObj);
                } catch (NumberFormatException e) {
                    return "Error: Invalid PID format - must be a number";
                }
            } else {
                return "Error: Invalid PID type - must be a number or string";
            }
            
            String formatStr = (String) arguments.getOrDefault("format", "JSON");
            ReportFormat format;
            try {
                format = ReportFormat.valueOf(formatStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return "Error: Unsupported format '" + formatStr + "'. Supported formats: JSON, XML, TEXT";
            }
            
            log.info("MCP: Generating and analyzing thread dump for PID: {}, format: {}", pid, format);
            
            // Generate thread dump
            String threadDump = threadDumpGenerationService.generateThreadDump(pid);
            
            // Analyze thread dump
            String source = "pid-" + pid;
            DiagnosticReport report = diagnosticService.analyzeThreadDump(threadDump, source);
            String formattedReport = reportFormatterService.formatReport(report, format);
            
            log.info("MCP: Thread dump generation and analysis completed successfully for PID: {}, report ID: {}", pid, report.id());
            
            return formattedReport;
            
        } catch (IllegalArgumentException e) {
            log.error("MCP: Invalid PID for thread dump generation and analysis", e);
            return "Error: " + e.getMessage();
        } catch (Exception e) {
            log.error("MCP: Error generating and analyzing thread dump", e);
            return "Error: Failed to generate and analyze thread dump - " + e.getMessage();
        }
    }
}