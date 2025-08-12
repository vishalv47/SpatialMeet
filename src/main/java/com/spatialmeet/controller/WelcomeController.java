package com.spatialmeet.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.List;
import java.util.Arrays;

@RestController
@RequestMapping("/api")
public class WelcomeController {
    
    @GetMapping
    public Map<String, Object> welcome() {
        return Map.of(
            "application", "SpatialMeet - Spatial Audio Conferencing Platform",
            "version", "1.0.0",
            "status", "Running",
            "message", "Welcome to SpatialMeet API",
            "documentation", Map.of(
                "authentication", Map.of(
                    "signup", "POST /api/auth/signup",
                    "signin", "POST /api/auth/signin", 
                    "signout", "POST /api/auth/signout"
                ),
                "rooms", Map.of(
                    "list_public", "GET /api/rooms",
                    "create", "POST /api/rooms",
                    "get_room", "GET /api/rooms/{roomCode}",
                    "join", "POST /api/rooms/{roomCode}/join",
                    "leave", "POST /api/rooms/{roomCode}/leave",
                    "participants", "GET /api/rooms/{roomCode}/participants"
                ),
                "websocket", "ws://localhost:8080/ws"
            ),
            "database_console", "http://localhost:8080/api/h2-console"
        );
    }
    
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
            "status", "UP",
            "timestamp", java.time.LocalDateTime.now().toString()
        );
    }
}
