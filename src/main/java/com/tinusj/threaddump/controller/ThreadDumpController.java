package com.tinusj.threaddump.controller;

import com.tinusj.threaddump.model.DiagnosticReport;
import com.tinusj.threaddump.enums.ReportFormat;
import com.tinusj.threaddump.service.DiagnosticService;
import com.tinusj.threaddump.service.ReportFormatterService;
import com.tinusj.threaddump.skill.ThreadDumpAnalysisSkill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * REST controller for thread dump analysis endpoints.
 */
@RestController
@RequestMapping("/thread-dump")
@Slf4j
public class ThreadDumpController {
    
    private final DiagnosticService diagnosticService;
    private final ReportFormatterService reportFormatterService;
    private final ThreadDumpAnalysisSkill mcpSkill;
    
    public ThreadDumpController(DiagnosticService diagnosticService, 
                              ReportFormatterService reportFormatterService,
                              ThreadDumpAnalysisSkill mcpSkill) {
        this.diagnosticService = diagnosticService;
        this.reportFormatterService = reportFormatterService;
        this.mcpSkill = mcpSkill;
    }
    
    /**
     * Analyzes thread dump content provided as text.
     * 
     * @param threadDumpContent the thread dump content as text
     * @param format the desired output format (default: JSON)
     * @return diagnostic report in the specified format
     */
    @PostMapping("/analyze-text")
    public ResponseEntity<String> analyzeThreadDumpText(
            @RequestBody String threadDumpContent,
            @RequestParam(defaultValue = "JSON") ReportFormat format) {
        
        log.info("Analyzing thread dump from text input, format: {}", format);
        
        try {
            if (threadDumpContent == null || threadDumpContent.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Thread dump content cannot be empty");
            }
            
            DiagnosticReport report = diagnosticService.analyzeThreadDump(threadDumpContent, "text-input");
            String formattedReport = reportFormatterService.formatReport(report, format);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, format.getContentType());
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(formattedReport);
                    
        } catch (IllegalArgumentException e) {
            log.warn("Invalid format requested: {}", format, e);
            return ResponseEntity.badRequest()
                    .body("Unsupported format: " + format);
        } catch (Exception e) {
            log.error("Error analyzing thread dump from text", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + e.getMessage());
        }
    }
    
    /**
     * Analyzes thread dump content provided as file upload.
     * 
     * @param file the thread dump file
     * @param format the desired output format (default: JSON)
     * @return diagnostic report in the specified format
     */
    @PostMapping("/analyze-file")
    public ResponseEntity<String> analyzeThreadDumpFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "JSON") ReportFormat format) {
        
        log.info("Analyzing thread dump from file: {}, format: {}", file.getOriginalFilename(), format);
        
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("File cannot be empty");
            }
            
            String threadDumpContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "uploaded-file";
            
            DiagnosticReport report = diagnosticService.analyzeThreadDump(threadDumpContent, filename);
            String formattedReport = reportFormatterService.formatReport(report, format);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, format.getContentType());
            headers.add("Content-Disposition", 
                    String.format("attachment; filename=\"report_%s%s\"", report.id(), format.getFileExtension()));
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(formattedReport);
                    
        } catch (IllegalArgumentException e) {
            log.warn("Invalid format requested: {}", format, e);
            return ResponseEntity.badRequest()
                    .body("Unsupported format: " + format);
        } catch (Exception e) {
            log.error("Error analyzing thread dump from file: {}", file.getOriginalFilename(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + e.getMessage());
        }
    }
    
    /**
     * MCP skill endpoint for analyzing thread dumps.
     * This provides MCP-compatible functionality via REST API.
     * 
     * @param arguments the MCP arguments containing content, format, and source
     * @return formatted diagnostic report
     */
    @PostMapping("/mcp-analyze")
    public ResponseEntity<String> mcpAnalyzeThreadDump(@RequestBody Map<String, Object> arguments) {
        log.info("MCP endpoint: Analyzing thread dump");
        
        try {
            String result = mcpSkill.handleAnalyzeThreadDump(arguments);
            
            // Determine content type based on format argument
            String formatStr = (String) arguments.getOrDefault("format", "JSON");
            ReportFormat format = ReportFormat.valueOf(formatStr.toUpperCase());
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, format.getContentType());
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(result);
                    
        } catch (Exception e) {
            log.error("MCP endpoint: Error analyzing thread dump", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Returns the MCP tool definition.
     * 
     * @return MCP tool definition JSON
     */
    @GetMapping("/mcp-tool-definition")
    public ResponseEntity<String> getMcpToolDefinition() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(mcpSkill.getMcpToolDefinition());
    }
    
    /**
     * Returns information about supported formats.
     * 
     * @return list of supported report formats
     */
    @GetMapping("/formats")
    public ResponseEntity<java.util.Set<ReportFormat>> getSupportedFormats() {
        return ResponseEntity.ok(reportFormatterService.getSupportedFormats());
    }
}