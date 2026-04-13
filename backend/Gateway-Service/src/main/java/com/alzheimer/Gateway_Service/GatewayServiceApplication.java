package com.alzheimer.Gateway_Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@SpringBootApplication
@EnableDiscoveryClient
public class GatewayServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayServiceApplication.class, args);
	}

	@Bean
	public RouteLocator gatewayRoutes(
			RouteLocatorBuilder builder,
			@Value("${gateway.direct.activite-educative-service-uri:}") String directActiviteUri) {
		String activiteTarget = StringUtils.hasText(directActiviteUri)
				? directActiviteUri.trim()
				: "lb://activite-educative-service";
		return builder.routes()
				.route("session-service", r -> r.path("/session/**")
						.uri("lb://SESSION-SERVICE"))
				.route("activite-uploads", r -> r.path("/uploads/**")
						.uri(activiteTarget))
				// Sessions jeu : chemins raccourcis (move, résultat) — même microservice que /api/activities
				.route("activite-game-sessions-flat", r -> r.path("/api/game-sessions", "/api/game-sessions/**")
						.uri(activiteTarget))
				// Deux motifs : liste exacte + tout sous-chemin (ex. /api/activities/4/start, .../game-sessions)
				.route("activite-educative-service", r -> r.path("/api/activities", "/api/activities/**")
						.uri(activiteTarget))
				.route("suivi-engagement-service", r -> r.path("/api/engagement", "/api/engagement/**")
						.uri("lb://SUIVI-ENGAGEMENT-SERVICE"))
				.route("event-service", r -> r.path("/api/events", "/api/events/**")
						.uri("lb://EVENT-SERVICE"))
				.route("maps-service", r -> r.path("/api/maps/**")
						.uri("lb://EVENT-SERVICE"))
				.route("emails-service", r -> r.path("/api/emails/**")
						.uri("lb://EVENT-SERVICE"))
				.build();
	}

	/**
	 * Configuration globale CORS pour le Gateway.
	 * Autorise les appels depuis l'application Angular en http://localhost:4200.
	 */
	@Bean
	public CorsWebFilter corsWebFilter() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.addAllowedOrigin("http://localhost:4200");
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);

		return new CorsWebFilter(source);
	}
}

