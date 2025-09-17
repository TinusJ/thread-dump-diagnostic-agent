package com.tinusj.threaddump.service;

import com.tinusj.threaddump.model.DiagnosticReport;
import com.tinusj.threaddump.model.ThreadInfo;

import java.util.List;

public interface DiagnosticAnalysisService {
    
    List<DiagnosticReport> analyzeThreadDump(List<ThreadInfo> threads);
    
    DiagnosticReport detectDeadlocks(List<ThreadInfo> threads);
    
    DiagnosticReport analyzePerformance(List<ThreadInfo> threads);
    
    List<String> generateRecommendations(List<DiagnosticReport> reports);
}