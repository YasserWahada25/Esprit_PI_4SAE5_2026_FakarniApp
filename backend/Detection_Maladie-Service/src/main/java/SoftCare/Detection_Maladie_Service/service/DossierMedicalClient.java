package SoftCare.Detection_Maladie_Service.service;

import SoftCare.Detection_Maladie_Service.dto.AjouterAnalyseRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Sends each new MRI analysis to Dossier_Medical-service.
 *
 * Replaces the former Feign client: with Spring Boot 4 / Spring Cloud 2025.1 its
 * encoder had no JSON converter ("no suitable HttpMessageConverter found for request
 * type AjouterAnalyseRequest"), so no analysis ever reached a patient's dossier.
 * A RestClient, like the one already used for the Flask AI service, serialises the
 * request with Jackson.
 */
@Component
public class DossierMedicalClient {

    private final RestClient restClient;

    public DossierMedicalClient(@Qualifier("dossierRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public void ajouterAnalyse(AjouterAnalyseRequest request) {
        restClient.post()
                .uri("/api/dossiers/ajouter-analyse")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
