package com.alzheimer.gateway_service.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // ═══════════════════════════════════════════════════════════
                //  AUTH ROUTES → user-service
                // ═══════════════════════════════════════════════════════════
                .route("api-auth", r -> r
                        .path("/auth/**")
                        .uri("lb://USER-SERVICE"))

                // ═══════════════════════════════════════════════════════════
                //  USER PROFILE ROUTES → user-service
                // ═══════════════════════════════════════════════════════════
                .route("api-user-profile", r -> r
                        .path("/api/profile/**")
                        .uri("lb://USER-SERVICE"))
                .route("uploads-all", r -> r
                        .path("/uploads/**")
                        .uri("lb://activite-educative-service"))
                
                // ═══════════════════════════════════════════════════════════
                //  API ROUTES
                // ═══════════════════════════════════════════════════════════
                .route("api-activities", r -> r
                        .path("/api/activities/**")
                        .uri("lb://activite-educative-service"))
                .route("api-events", r -> r
                        .path("/api/events/**")
                        .uri("lb://activite-educative-service"))
                .route("api-engagement", r -> r
                        .path("/api/engagement/**")
                        .uri("lb://activite-educative-service"))
                .route("api-game-sessions", r -> r
                        .path("/api/game-sessions/**")
                        .uri("lb://activite-educative-service"))
                
                // ═══════════════════════════════════════════════════════════
                //  DETECTION & DOSSIER MEDICAL
                // ═══════════════════════════════════════════════════════════
                .route("api-detection", r -> r
                        .path("/api/detection/**")
                        .uri("lb://detection-maladie-service"))
                .route("api-dossiers", r -> r
                        .path("/api/dossiers/**")
                        .uri("lb://dossier-medical-service"))
                
                // ═══════════════════════════════════════════════════════════
                //  POSTS
                // ═══════════════════════════════════════════════════════════
                .route("api-posts", r -> r
                        .path("/api/posts/**")
                        .uri("lb://post-service"))
                
                // ═══════════════════════════════════════════════════════════
                //  GROUPS
                // ═══════════════════════════════════════════════════════════
                .route("api-groups", r -> r
                        .path("/api/groups/**")
                        .uri("lb://group-service"))
                
                // ═══════════════════════════════════════════════════════════
                //  GEOFENCING
                // ═══════════════════════════════════════════════════════════
                .route("api-geofencing", r -> r
                        .path("/api/geofencing/**")
                        .uri("lb://geofencing-service"))
                
                // ═══════════════════════════════════════════════════════════
                //  USERS (user management → user-service)
                // ═══════════════════════════════════════════════════════════
                .route("api-users", r -> r
                        .path("/api/users/**")
                        .uri("lb://USER-SERVICE"))

                // ═══════════════════════════════════════════════════════════
                //  SESSIONS (session management → session-service)
                // ═══════════════════════════════════════════════════════════
                .route("api-sessions", r -> r
                        .path("/api/sessions/**")
                        .uri("lb://SESSION-SERVICE"))
                
                // ═══════════════════════════════════════════════════════════
                //  CHAT
                // ═══════════════════════════════════════════════════════════
                .route("api-chat", r -> r
                        .path("/api/chat/**")
                        .uri("lb://chat-service"))
                
                .build();
    }
}
