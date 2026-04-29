package com.alzheimer.group_service.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class JwtServiceTest {

    @Test
    void classShouldBeLoadable() {
        assertNotNull(JwtService.class);
    }
}
