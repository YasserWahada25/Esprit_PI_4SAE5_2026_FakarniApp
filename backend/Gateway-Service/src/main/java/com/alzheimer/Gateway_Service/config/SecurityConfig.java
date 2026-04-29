package com.alzheimer.Gateway_Service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                // CORS uniquement via CorsConfig.corsWebFilter() — évite des en-têtes dupliqués.
                .authorizeExchange(ex -> ex
                        .anyExchange().permitAll()
                )
                .build();
    }
}
