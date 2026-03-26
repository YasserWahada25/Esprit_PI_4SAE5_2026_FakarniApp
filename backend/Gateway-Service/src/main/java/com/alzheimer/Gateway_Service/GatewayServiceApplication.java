package com.alzheimer.Gateway_Service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@SpringBootApplication
@EnableDiscoveryClient
public class GatewayServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayServiceApplication.class, args);
	}

	@Bean
	public RouteLocator gatewayRoutes(
			RouteLocatorBuilder builder,
			@Value("${gateway.routes.user-auth.uri:lb://USER-SERVICE}") String userAuthUri,
			@Value("${gateway.routes.session.uri:http://localhost:8085}") String sessionUri,
			@Value("${gateway.routes.event.uri:lb://EVENT-SERVICE}") String eventUri,
			@Value("${gateway.routes.user.uri:lb://USER-SERVICE}") String userUri
	) {
		return builder.routes()
				.route("user-auth", r -> r.path("/auth/**", "/internal/users/**")
						.uri(userAuthUri))
				.route("session_service", r -> r.path("/session/**")
						.uri(sessionUri))
				.route("session_ws", r -> r.path("/ws/**")
						.uri(sessionUri))
				.route("Event-Service", r -> r.path("/api/events/**")
						.uri(eventUri))
				.route("user-service", r -> r.path("/api/users", "/api/users/**")
						.uri(userUri))
				.build();
	}

	@Bean
	public CorsWebFilter corsWebFilter() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(List.of("http://localhost:4200", "http://localhost:3000"));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return new CorsWebFilter(source);
	}
}
