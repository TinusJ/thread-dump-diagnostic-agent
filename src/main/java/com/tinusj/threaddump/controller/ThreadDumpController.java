package com.tinusj.threaddump.controller;

import com.tinusj.threaddump.model.ThreadDumpRequest;
import com.tinusj.threaddump.model.ThreadDumpResponse;
import com.tinusj.threaddump.service.ThreadDumpService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/thread-dumps")
public class ThreadDumpController {

    private final ThreadDumpService threadDumpService;

    public ThreadDumpController(ThreadDumpService threadDumpService) {
        this.threadDumpService = threadDumpService;
    }

    @PostMapping
    public ResponseEntity<ThreadDumpResponse> generateThreadDump(@Valid @RequestBody ThreadDumpRequest request) {
        try {
            ThreadDumpResponse response = threadDumpService.generateThreadDump(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{dumpId}")
    public ResponseEntity<ThreadDumpResponse> getThreadDump(@PathVariable String dumpId) {
        try {
            ThreadDumpResponse response = threadDumpService.getThreadDump(dumpId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ThreadDumpResponse>> getAllThreadDumps() {
        try {
            List<ThreadDumpResponse> responses = threadDumpService.getAllThreadDumps();
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{dumpId}")
    public ResponseEntity<Void> deleteThreadDump(@PathVariable String dumpId) {
        try {
            threadDumpService.deleteThreadDump(dumpId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}