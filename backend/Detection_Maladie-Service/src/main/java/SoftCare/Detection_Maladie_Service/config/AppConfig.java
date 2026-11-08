package SoftCare.Detection_Maladie_Service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AppConfig {

    @Value("${ia.flask.url:http://localhost:5000}")
    private String flaskUrl;

    /**
     * RestClient dédié à l'appel du service Flask Python (IA - prédiction IRM).
     * L'URL est configurée via la propriété ia.flask.url.
     * Dossier_Medical-service est appelé via dossierRestClient (voir DossierMedicalClient).
     */
    /** Dossier_Medical-service, joint par son nom de service Docker (surchargeable). */
    @Value("${dossier-medical.url:http://dossier-medical-service:8059}")
    private String dossierMedicalUrl;

    @Bean("dossierRestClient")
    public RestClient dossierRestClient() {
        return RestClient.builder().baseUrl(dossierMedicalUrl).build();
    }

    @Bean("flaskRestClient")
    public RestClient flaskRestClient() {
        return RestClient.builder().baseUrl(flaskUrl).build();
    }
}
