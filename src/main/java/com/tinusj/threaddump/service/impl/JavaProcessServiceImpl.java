package com.tinusj.threaddump.service.impl;

import com.tinusj.threaddump.model.JavaProcess;
import com.tinusj.threaddump.service.JavaProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of JavaProcessService using JPS (Java Process Status) tool.
 */
@Service
@Slf4j
public class JavaProcessServiceImpl implements JavaProcessService {

    private static final String JPS_COMMAND = "jps";
    private static final String JPS_VERBOSE_COMMAND = "jps -v";
    
    @Override
    public List<JavaProcess> getRunningJavaProcesses() {
        log.info("Detecting running Java processes using jps");
        
        List<JavaProcess> processes = new ArrayList<>();
        
        try {
            // Use jps -v to get detailed information including JVM arguments
            ProcessBuilder processBuilder = new ProcessBuilder(JPS_COMMAND, "-v");
            Process process = processBuilder.start();
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    JavaProcess javaProcess = parseJpsLine(line);
                    if (javaProcess != null) {
                        processes.add(javaProcess);
                    }
                }
            }
            
            // Wait for process completion
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.warn("jps command exited with code: {}", exitCode);
            }
            
            log.info("Found {} Java processes", processes.size());
            
        } catch (IOException | InterruptedException e) {
            log.error("Error running jps command to detect Java processes", e);
        }
        
        return processes;
    }

    @Override
    public JavaProcess getJavaProcessByPid(long pid) {
        log.info("Getting Java process information for PID: {}", pid);
        
        try {
            // Use jps to get process info directly for the specific PID
            ProcessBuilder processBuilder = new ProcessBuilder(JPS_COMMAND, "-v");
            Process process = processBuilder.start();
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    JavaProcess javaProcess = parseJpsLine(line);
                    if (javaProcess != null && javaProcess.pid() == pid) {
                        return javaProcess;
                    }
                }
            }
            
            // Wait for process completion
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.warn("jps command exited with code: {}", exitCode);
            }
            
        } catch (IOException | InterruptedException e) {
            log.error("Error running jps command to get Java process for PID: {}", pid, e);
        }
        
        return null;
    }
    
    /**
     * Parses a line from jps -v output to extract process information.
     * 
     * Expected format: "PID MainClass JVMArgs"
     * Example: "12345 com.example.MyApp -Xmx1g -Dfile.encoding=UTF-8"
     */
    private JavaProcess parseJpsLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        
        try {
            line = line.trim();
            
            // Skip jps process itself (contains "Jps" or "sun.tools.jps.Jps")
            if (line.contains("Jps") || line.contains("sun.tools.jps.Jps")) {
                return null;
            }
            
            // Split by space, first part is PID
            String[] parts = line.split("\\s+", 2);
            if (parts.length < 2) {
                return null;
            }
            
            long pid = Long.parseLong(parts[0]);
            String remaining = parts[1];
            
            // Extract main class and arguments
            String[] classParts = remaining.split("\\s+", 2);
            String mainClass = classParts[0];
            String arguments = classParts.length > 1 ? classParts[1] : "";
            
            // Separate JVM arguments from application arguments
            String jvmArguments = "";
            String applicationArguments = "";
            
            if (!arguments.isEmpty()) {
                // JVM arguments typically start with -
                StringBuilder jvmArgs = new StringBuilder();
                StringBuilder appArgs = new StringBuilder();
                
                String[] args = arguments.split("\\s+");
                for (String arg : args) {
                    if (arg.startsWith("-D") || arg.startsWith("-X") || arg.startsWith("-XX") 
                        || arg.startsWith("-server") || arg.startsWith("-client")
                        || arg.startsWith("-javaagent")) {
                        if (jvmArgs.length() > 0) jvmArgs.append(" ");
                        jvmArgs.append(arg);
                    } else {
                        if (appArgs.length() > 0) appArgs.append(" ");
                        appArgs.append(arg);
                    }
                }
                
                jvmArguments = jvmArgs.toString();
                applicationArguments = appArgs.toString();
            }
            
            // Create display name (use main class or jar name)
            String displayName = mainClass;
            if (mainClass.endsWith(".jar")) {
                // Extract jar name without path
                int lastSlash = mainClass.lastIndexOf('/');
                if (lastSlash >= 0) {
                    displayName = mainClass.substring(lastSlash + 1);
                }
            }
            
            return new JavaProcess(pid, mainClass, displayName, jvmArguments, applicationArguments);
            
        } catch (NumberFormatException e) {
            log.warn("Failed to parse PID from jps line: {}", line);
            return null;
        } catch (Exception e) {
            log.warn("Failed to parse jps line: {}", line, e);
            return null;
        }
    }
}