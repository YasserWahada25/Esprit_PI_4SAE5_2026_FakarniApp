package SoftCare.Detection_Maladie_Service.service;

import SoftCare.Detection_Maladie_Service.dto.AjouterAnalyseRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * The analysis must reach Dossier_Medical-service as JSON: the former Feign client
 * failed with "no suitable HttpMessageConverter found for request type
 * AjouterAnalyseRequest", so no dossier was ever filled.
 */
class DossierMedicalClientTest {

    @Test
    void sendsTheAnalysisAsJsonToDossierMedical() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://dossier-medical-service:8059");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://dossier-medical-service:8059/api/dossiers/ajouter-analyse"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.analyseIrmId").value(7))
                .andExpect(jsonPath("$.patientId").value("6af03c5566019019914eaaa2"))
                .andExpect(jsonPath("$.prediction").value("Non_Demented"))
                .andExpect(jsonPath("$.confidence").value(76.07))
                .andExpect(jsonPath("$.probNonDemented").value(76.07))
                .andExpect(jsonPath("$.dateAnalyse").exists())
                .andRespond(withSuccess());

        new DossierMedicalClient(builder.build()).ajouterAnalyse(AjouterAnalyseRequest.builder()
                .analyseIrmId(7L)
                .patientId("6af03c5566019019914eaaa2")
                .nomFichier("1731082657000_alz.jpg")
                .prediction("Non_Demented")
                .confidence(76.07)
                .niveauRisque("AUCUN")
                .couleurRisque("GREEN")
                .descriptionRisque("Aucun signe de démence détecté.")
                .probMildDemented(2.58)
                .probModerateDemented(1.47)
                .probNonDemented(76.07)
                .probVeryMildDemented(19.88)
                .dateAnalyse(LocalDateTime.of(2026, 11, 8, 16, 17, 37))
                .build());

        server.verify();
    }
}
