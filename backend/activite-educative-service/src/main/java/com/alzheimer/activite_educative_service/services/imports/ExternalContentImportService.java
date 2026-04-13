package com.alzheimer.activite_educative_service.services.imports;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Point d’extension pour brancher des APIs externes (quiz / banques d’images).
 * Si aucune URL n’est configurée ou si l’appel échoue, on retombe sur des données classpath.
 */
@Service
public class ExternalContentImportService {

    private static final Logger log = LoggerFactory.getLogger(ExternalContentImportService.class);

    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    @Value("${fakarni.import.quiz-url:}")
    private String quizImportUrl;

    @Value("${fakarni.import.image-bank-url:}")
    private String imageBankUrl;

    public ExternalContentImportService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.create();
    }

    /** Libellés d’images pour enrichir un memory (hors paires). */
    public List<String> fetchRemoteImageLabels() {
        if (imageBankUrl == null || imageBankUrl.isBlank()) {
            return readClasspathImageFallback();
        }
        try {
            String body = restClient.get().uri(imageBankUrl).retrieve().body(String.class);
            if (body == null || body.isBlank()) {
                return readClasspathImageFallback();
            }
            JsonNode root = objectMapper.readTree(body);
            if (root.isArray()) {
                List<String> out = new ArrayList<>();
                for (JsonNode n : root) {
                    if (n.isTextual()) {
                        out.add(n.asText());
                    } else if (n.has("label")) {
                        out.add(n.get("label").asText());
                    }
                }
                return out.isEmpty() ? readClasspathImageFallback() : out;
            }
        } catch (Exception e) {
            log.warn("Image bank URL failed ({}), using classpath fallback", imageBankUrl, e);
        }
        return readClasspathImageFallback();
    }

    public Optional<String> fetchRemoteQuizJson() {
        if (quizImportUrl == null || quizImportUrl.isBlank()) {
            return Optional.empty();
        }
        try {
            String body = restClient.get().uri(quizImportUrl).retrieve().body(String.class);
            return Optional.ofNullable(body).filter(s -> !s.isBlank());
        } catch (Exception e) {
            log.warn("Quiz import URL failed: {}", quizImportUrl, e);
            return Optional.empty();
        }
    }

    private List<String> readClasspathImageFallback() {
        try {
            ClassPathResource res = new ClassPathResource("data/demo-image-labels.json");
            if (!res.exists()) {
                return Collections.emptyList();
            }
            try (InputStream in = res.getInputStream()) {
                JsonNode root = objectMapper.readTree(in);
                if (!root.isArray()) {
                    return Collections.emptyList();
                }
                List<String> out = new ArrayList<>();
                for (JsonNode n : root) {
                    out.add(n.asText());
                }
                return out;
            }
        } catch (Exception e) {
            log.debug("No demo-image-labels.json or unreadable", e);
            return Collections.emptyList();
        }
    }
}
