package com.example.artship.social.controller;



import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    
    @GetMapping("/test")
    public String test() {
        return "🎉 Social API is running! Artship platform is working!";
    }
    
    @GetMapping("/health")
    public String health() {
        return "✅ OK - Server is healthy";
    }
    
    @GetMapping("/")
    public String home() {
        return "Welcome to Artship Social API! Use /test or /health endpoints";
    }
}