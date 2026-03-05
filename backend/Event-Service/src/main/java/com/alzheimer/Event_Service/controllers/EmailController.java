package com.alzheimer.Event_Service.controllers;

import com.alzheimer.Event_Service.services.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/emails")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/test")
    public ResponseEntity<?> sendTestEmail() {
        try {
            emailService.sendTestEmail();
            return ResponseEntity.ok(Map.of("message", "Test email sent successfully to testuser@example.com"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error sending test email: " + e.getMessage()));
        }
    }
}
