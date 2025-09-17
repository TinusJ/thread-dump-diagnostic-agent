package com.tinusj.threaddump.formatter;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tinusj.threaddump.model.DiagnosticReport;
import com.tinusj.threaddump.model.ReportFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Formats diagnostic reports as XML.
 */
@Component
@Slf4j
public class XmlReportFormatter implements ReportFormatter {
    
    private final XmlMapper xmlMapper;
    
    public XmlReportFormatter() {
        this.xmlMapper = new XmlMapper();
        this.xmlMapper.registerModule(new JavaTimeModule());
        this.xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.xmlMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
    
    @Override
    public String format(DiagnosticReport report) {
        try {
            return xmlMapper.writeValueAsString(report);
        } catch (Exception e) {
            log.error("Error formatting report as XML", e);
            return "<error>Failed to format report as XML: " + e.getMessage() + "</error>";
        }
    }
    
    @Override
    public ReportFormat getFormat() {
        return ReportFormat.XML;
    }
}