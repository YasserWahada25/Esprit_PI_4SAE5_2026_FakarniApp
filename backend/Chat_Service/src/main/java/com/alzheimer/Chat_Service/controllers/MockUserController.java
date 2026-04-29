package com.alzheimer.Chat_Service.controllers;

import com.alzheimer.Chat_Service.models.MockUser;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/mock-users")
public class MockUserController {

    private final List<MockUser> mockUsers = Arrays.asList(
            new MockUser("user1", "Dr. Sarah Martin", "sarah.martin@hospital.com", "Doctor", "fa-user-doctor"),
            new MockUser("user2", "John Anderson", "john.anderson@care.com", "Caregiver", "fa-user"),
            new MockUser("user3", "Nurse Emma Wilson", "emma.wilson@hospital.com", "Nurse", "fa-user-nurse"),
            new MockUser("user4", "Dr. Michael Chen", "michael.chen@hospital.com", "Doctor", "fa-user-doctor")
    );

    @GetMapping
    public List<MockUser> getAllUsers() {
        return mockUsers;
    }

    @GetMapping("/{userId}")
    public MockUser getUserById(@PathVariable String userId) {
        return mockUsers.stream()
                .filter(user -> user.getId().equals(userId))
                .findFirst()
                .orElse(null);
    }
}
