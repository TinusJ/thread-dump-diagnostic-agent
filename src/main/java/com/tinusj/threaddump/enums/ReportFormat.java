package com.tinusj.threaddump.enums;

/**
 * Enumeration representing the available output formats for diagnostic reports.
 */
public enum ReportFormat {
    JSON("application/json", ".json"),
    XML("application/xml", ".xml"),
    TEXT("text/plain", ".txt");

    private final String contentType;
    private final String fileExtension;

    ReportFormat(String contentType, String fileExtension) {
        this.contentType = contentType;
        this.fileExtension = fileExtension;
    }

    public String getContentType() {
        return contentType;
    }

    public String getFileExtension() {
        return fileExtension;
    }
}