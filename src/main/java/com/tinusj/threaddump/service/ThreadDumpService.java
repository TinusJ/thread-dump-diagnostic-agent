package com.tinusj.threaddump.service;

import com.tinusj.threaddump.model.ThreadDumpRequest;
import com.tinusj.threaddump.model.ThreadDumpResponse;

import java.util.List;

public interface ThreadDumpService {
    
    ThreadDumpResponse generateThreadDump(ThreadDumpRequest request);
    
    ThreadDumpResponse getThreadDump(String dumpId);
    
    List<ThreadDumpResponse> getAllThreadDumps();
    
    void deleteThreadDump(String dumpId);
}