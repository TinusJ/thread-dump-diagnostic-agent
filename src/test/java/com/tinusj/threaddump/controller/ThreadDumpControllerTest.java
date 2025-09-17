package com.tinusj.threaddump.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinusj.threaddump.enums.ThreadState;
import com.tinusj.threaddump.model.ThreadDumpRequest;
import com.tinusj.threaddump.model.ThreadDumpResponse;
import com.tinusj.threaddump.model.ThreadInfo;
import com.tinusj.threaddump.service.ThreadDumpService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ThreadDumpController.class, 
            excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, 
                                                   classes = com.tinusj.threaddump.exception.GlobalExceptionHandler.class))
class ThreadDumpControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ThreadDumpService threadDumpService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void generateThreadDump_WithValidRequest_ShouldReturnCreated() throws Exception {
        // Given
        ThreadDumpRequest request = new ThreadDumpRequest("12345", 30, true, true);
        ThreadDumpResponse response = new ThreadDumpResponse(
            "dump-123",
            LocalDateTime.now(),
            "12345",
            1,
            List.of(new ThreadInfo(1L, "main", ThreadState.RUNNABLE, 1000L, 0L, 0L, null, null, null, List.of())),
            List.of()
        );

        when(threadDumpService.generateThreadDump(any(ThreadDumpRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/thread-dumps")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dumpId").value("dump-123"))
                .andExpect(jsonPath("$.processId").value("12345"))
                .andExpect(jsonPath("$.totalThreads").value(1));
    }

    @Test
    void generateThreadDump_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given - invalid request with blank processId
        ThreadDumpRequest invalidRequest = new ThreadDumpRequest("", 30, true, true);

        // When & Then
        mockMvc.perform(post("/thread-dumps")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getThreadDump_WithValidId_ShouldReturnOk() throws Exception {
        // Given
        String dumpId = "dump-123";
        ThreadDumpResponse response = new ThreadDumpResponse(
            dumpId,
            LocalDateTime.now(),
            "12345",
            1,
            List.of(),
            List.of()
        );

        when(threadDumpService.getThreadDump(dumpId)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/thread-dumps/{dumpId}", dumpId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dumpId").value(dumpId))
                .andExpect(jsonPath("$.processId").value("12345"));
    }

    @Test
    void getThreadDump_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Given
        String invalidId = "invalid-id";
        when(threadDumpService.getThreadDump(invalidId)).thenThrow(new IllegalArgumentException("Thread dump not found"));

        // When & Then
        mockMvc.perform(get("/thread-dumps/{dumpId}", invalidId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllThreadDumps_ShouldReturnOk() throws Exception {
        // Given
        List<ThreadDumpResponse> responses = List.of(
            new ThreadDumpResponse("dump-1", LocalDateTime.now(), "12345", 1, List.of(), List.of()),
            new ThreadDumpResponse("dump-2", LocalDateTime.now(), "67890", 2, List.of(), List.of())
        );

        when(threadDumpService.getAllThreadDumps()).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/thread-dumps"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void deleteThreadDump_WithValidId_ShouldReturnNoContent() throws Exception {
        // Given
        String dumpId = "dump-123";

        // When & Then
        mockMvc.perform(delete("/thread-dumps/{dumpId}", dumpId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteThreadDump_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Given
        String invalidId = "invalid-id";
        doThrow(new IllegalArgumentException("Thread dump not found"))
            .when(threadDumpService).deleteThreadDump(invalidId);

        // When & Then
        mockMvc.perform(delete("/thread-dumps/{dumpId}", invalidId))
                .andExpect(status().isNotFound());
    }
}