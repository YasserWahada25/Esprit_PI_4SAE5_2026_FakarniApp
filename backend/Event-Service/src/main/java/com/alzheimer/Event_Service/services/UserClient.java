package com.alzheimer.event_service.services;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * Client Feign vers la Gateway ou le User-Service (identifiants utilisateur Mongo = {@link String}).
 */
@FeignClient(name = "user-service", url = "${feign.user-service.url:http://localhost:8090}")
public interface UserClient {

    @GetMapping("/api/users/{id}")
    Map<String, Object> getUserById(@PathVariable("id") String id);
}
