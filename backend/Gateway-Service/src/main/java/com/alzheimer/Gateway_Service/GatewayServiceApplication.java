package com.alzheimer.Gateway_Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

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
			@Value("${gateway.routes.session.uri:lb://SESSION-SERVICE}") String sessionUri,
			@Value("${gateway.routes.event.uri:lb://EVENT-SERVICE}") String eventUri,
			@Value("${gateway.routes.user.uri:lb://USER-SERVICE}") String userUri,
			@Value("${gateway.routes.post.uri:lb://POST-SERVICE}") String postUri,
			@Value("${gateway.routes.group.uri:lb://GROUP-SERVICE}") String groupUri,
			@Value("${gateway.routes.chat.uri:lb://CHAT-SERVICE}") String chatUri,
			@Value("${gateway.routes.chat-ws.uri:lb:ws://CHAT-SERVICE}") String chatWsUri,
			@Value("${gateway.routes.meeting-insights.uri:lb://MEETING-INSIGHTS-SERVICE}") String meetingInsightsUri,
			@Value("${gateway.direct.activite-educative-service-uri:}") String directActiviteUri) {
		String activiteTarget = StringUtils.hasText(directActiviteUri)
				? directActiviteUri.trim()
				: "lb://activite-educative-service";
		return builder.routes()
				.route("Detection_Maladie-Service", r ->
						r.path("/api/detection/**")
								.uri("lb://Detection_Maladie-Service"))
				.route("Dossier_Medical-Service", r ->
						r.path("/api/dossiers/**")
								.uri("lb://Dossier_Medical-Service"))
				.route("user-auth", r ->
						r.path("/auth/**", "/internal/users/**")
								.uri(userAuthUri))
				.route("user-service", r ->
						r.path("/api/users", "/api/users/**")
								.uri(userUri))
				.route("session_service", r ->
						r.path("/session/**")
								.uri(sessionUri))
				.route("session_ws", r ->
						r.path("/ws/**")
								.uri(sessionUri))
				.route("activite-uploads", r -> r.path("/uploads/**")
						.uri(activiteTarget))
				.route("activite-game-sessions-flat", r -> r.path("/api/game-sessions", "/api/game-sessions/**")
						.uri(activiteTarget))
				.route("activite-educative-service", r -> r.path("/api/activities", "/api/activities/**")
						.uri(activiteTarget))
				.route("suivi-engagement-service", r -> r.path("/api/engagement", "/api/engagement/**")
						.uri("lb://SUIVI-ENGAGEMENT-SERVICE"))
				.route("Event-Service", r ->
						r.path("/api/events", "/api/events/**")
								.uri(eventUri))
				.route("maps-service", r -> r.path("/api/maps/**")
						.uri(eventUri))
				.route("emails-service", r -> r.path("/api/emails/**")
						.uri(eventUri))
				.route("Post-Service", r ->
						r.path("/api/posts/**")
								.uri(postUri))
				.route("Group-Service", r ->
						r.path("/api/groups/**")
								.uri(groupUri))
				.route("Chat-Service-Messages", r ->
						r.path("/api/messages/**")
								.uri(chatUri))
				.route("Chat-Service-MockUsers", r ->
						r.path("/api/mock-users/**")
								.uri(chatUri))
				.route("Chat-Service-WebSocket", r ->
						r.path("/chat-ws/**")
								.uri(chatWsUri))
				.route("meeting-insights-service", r ->
						r.path("/api/meet/**")
								.uri(meetingInsightsUri))
				.route("tracking_route", r -> r.path("/api/tracking/**")
						.uri("lb://TRACKING-SERVICE"))
				.route("geofencing_route", r -> r.path("/api/geofencing/**")
						.uri("lb://GEOFENCING-SERVICE"))
				.build();
	}
}
