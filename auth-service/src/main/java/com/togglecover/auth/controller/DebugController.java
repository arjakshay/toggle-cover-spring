package com.togglecover.auth.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/debug")
@Slf4j
public class DebugController {

    @PostMapping("/test")
    public Map<String, String> testEndpoint(@RequestBody Map<String, String> request) {
        log.info("Debug endpoint called with request: {}", request);
        return Map.of("status", "ok", "message", "Request received");
    }

    @GetMapping("/headers")
    public Map<String, Object> getHeaders(@RequestHeader Map<String, String> headers) {
        log.info("Headers received: {}", headers);
        return Map.of("headers", headers);
    }
}