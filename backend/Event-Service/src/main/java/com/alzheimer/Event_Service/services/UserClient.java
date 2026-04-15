package com.alzheimer.event_service.services;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * Interface Feign pour communiquer avec User-Service (ou Gateway).
 * L'URL pointe soit vers le Gateway (8090) soit vers l'Eureka ID du User Service "user-service".
 * Assurez-vous que l'endpoint correspond à votre architecture réelle.
 */
@FeignClient(name = "user-service", url = "http://localhost:8090") // Utilise Gateway par défaut
public interface UserClient {

    @GetMapping("/api/users/{id}")
    Map<String, Object> getUserById(@PathVariable("id") Long id);
}
