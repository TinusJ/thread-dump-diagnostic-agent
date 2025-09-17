package com.tinusj.threaddump.service.impl;

import com.tinusj.threaddump.enums.ThreadState;
import com.tinusj.threaddump.model.DiagnosticReport;
import com.tinusj.threaddump.model.ThreadDumpRequest;
import com.tinusj.threaddump.model.ThreadDumpResponse;
import com.tinusj.threaddump.model.ThreadInfo;
import com.tinusj.threaddump.service.DiagnosticAnalysisService;
import com.tinusj.threaddump.service.ThreadDumpService;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ThreadDumpServiceImpl implements ThreadDumpService {

    private final DiagnosticAnalysisService diagnosticAnalysisService;
    private final Map<String, ThreadDumpResponse> threadDumps = new ConcurrentHashMap<>();

    public ThreadDumpServiceImpl(DiagnosticAnalysisService diagnosticAnalysisService) {
        this.diagnosticAnalysisService = diagnosticAnalysisService;
    }

    @Override
    public ThreadDumpResponse generateThreadDump(ThreadDumpRequest request) {
        String dumpId = UUID.randomUUID().toString();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        
        List<ThreadInfo> threads = new ArrayList<>();
        java.lang.management.ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(
            request.includeStackTraces(), 
            false
        );

        for (java.lang.management.ThreadInfo threadInfo : threadInfos) {
            ThreadInfo thread = new ThreadInfo(
                threadInfo.getThreadId(),
                threadInfo.getThreadName(),
                mapToThreadState(threadInfo.getThreadState()),
                threadMXBean.getThreadCpuTime(threadInfo.getThreadId()),
                threadInfo.getBlockedTime(),
                threadInfo.getWaitedTime(),
                threadInfo.getLockName(),
                threadInfo.getLockOwnerName(),
                threadInfo.getLockOwnerId() == -1 ? null : threadInfo.getLockOwnerId(),
                threadInfo.getStackTrace() != null ? Arrays.asList(threadInfo.getStackTrace()) : List.of()
            );
            threads.add(thread);
        }

        List<DiagnosticReport> diagnostics = request.analyzePerformance() ? 
            diagnosticAnalysisService.analyzeThreadDump(threads) : List.of();

        ThreadDumpResponse response = new ThreadDumpResponse(
            dumpId,
            LocalDateTime.now(),
            request.processId(),
            threads.size(),
            threads,
            diagnostics
        );

        threadDumps.put(dumpId, response);
        return response;
    }

    @Override
    public ThreadDumpResponse getThreadDump(String dumpId) {
        ThreadDumpResponse response = threadDumps.get(dumpId);
        if (response == null) {
            throw new IllegalArgumentException("Thread dump not found: " + dumpId);
        }
        return response;
    }

    @Override
    public List<ThreadDumpResponse> getAllThreadDumps() {
        return new ArrayList<>(threadDumps.values());
    }

    @Override
    public void deleteThreadDump(String dumpId) {
        if (!threadDumps.containsKey(dumpId)) {
            throw new IllegalArgumentException("Thread dump not found: " + dumpId);
        }
        threadDumps.remove(dumpId);
    }

    private ThreadState mapToThreadState(Thread.State state) {
        return switch (state) {
            case NEW -> ThreadState.NEW;
            case RUNNABLE -> ThreadState.RUNNABLE;
            case BLOCKED -> ThreadState.BLOCKED;
            case WAITING -> ThreadState.WAITING;
            case TIMED_WAITING -> ThreadState.TIMED_WAITING;
            case TERMINATED -> ThreadState.TERMINATED;
        };
    }
}