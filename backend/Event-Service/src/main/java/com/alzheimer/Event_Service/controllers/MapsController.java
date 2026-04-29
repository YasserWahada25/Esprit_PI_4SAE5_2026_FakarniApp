package com.alzheimer.event_service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/maps")
public class MapsController {

    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Geocode a location text using OpenStreetMap Nominatim (free, no API key).
     * GET /api/maps/geocode?query=tunis
     * Response: { query, lat, lng, formattedAddress }
     */
    @GetMapping("/geocode")
    public ResponseEntity<?> geocode(@RequestParam String query) {
        try {
            URI uri = UriComponentsBuilder.fromUriString(NOMINATIM_URL)
                    .queryParam("q", query)
                    .queryParam("format", "json")
                    .queryParam("limit", 1)
                    .queryParam("addressdetails", 1)
                    .queryParam("countrycodes", "tn") // Restrict to Tunisia
                    .build()
                    .encode()
                    .toUri();

            // Nominatim requires a User-Agent header
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", "FakarniApp/1.0 (contact@fakarni.com)");
            org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> results = restTemplate.exchange(
                    uri,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    (Class<List<Map<String, Object>>>) (Class<?>) List.class
            ).getBody();

            if (results == null || results.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of(
                        "error", "Location not found",
                        "query", query
                ));
            }

            Map<String, Object> first = results.get(0);
            double lat = Double.parseDouble((String) first.get("lat"));
            double lng = Double.parseDouble((String) first.get("lon"));
            String displayName = (String) first.get("display_name");

            return ResponseEntity.ok(Map.of(
                    "query", query,
                    "lat", lat,
                    "lng", lng,
                    "formattedAddress", displayName != null ? displayName : query
            ));

        } catch (Exception e) {
            return ResponseEntity.status(502).body(Map.of(
                    "error", "Geocoding service unavailable: " + e.getMessage(),
                    "query", query
            ));
        }
    }

    /**
     * Reverse Geocode: convert lat/lng coordinates to a human-readable address.
     * GET /api/maps/reverse-geocode?lat=36.8065&lng=10.1815
     */
    @GetMapping("/reverse-geocode")
    public ResponseEntity<?> reverseGeocode(@RequestParam double lat, @RequestParam double lng) {
        try {
            URI uri = UriComponentsBuilder.fromUriString("https://nominatim.openstreetmap.org/reverse")
                    .queryParam("lat", lat)
                    .queryParam("lon", lng)
                    .queryParam("format", "json")
                    .queryParam("addressdetails", 1)
                    .build()
                    .encode()
                    .toUri();

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", "FakarniApp/1.0 (contact@fakarni.com)");
            org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> result = restTemplate.exchange(
                    uri,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    (Class<Map<String, Object>>) (Class<?>) Map.class
            ).getBody();

            if (result == null || !result.containsKey("display_name")) {
                return ResponseEntity.status(404).body(Map.of("error", "Address not found"));
            }

            return ResponseEntity.ok(Map.of(
                    "lat", lat,
                    "lng", lng,
                    "formattedAddress", result.get("display_name")
            ));

        } catch (Exception e) {
            return ResponseEntity.status(502).body(Map.of(
                    "error", "Reverse geocoding service unavailable: " + e.getMessage()
            ));
        }
    }
}
