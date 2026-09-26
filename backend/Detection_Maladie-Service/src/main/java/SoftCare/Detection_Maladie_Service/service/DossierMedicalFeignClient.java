package SoftCare.Detection_Maladie_Service.service;

import SoftCare.Detection_Maladie_Service.dto.AjouterAnalyseRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Client Feign vers le Dossier_Medical-service.
 * Feign refuse un "_" dans name (« Service id not legal hostname »), or le service s'enregistre sous
 * Dossier_Medical-Service : on l'adresse donc par URL, surchargeable via DOSSIER_MEDICAL_URL.
 */
@FeignClient(name = "dossier-medical-client", url = "${dossier-medical.url:http://dossier-medical-service:8059}")
public interface DossierMedicalFeignClient {

    @PostMapping("/api/dossiers/ajouter-analyse")
    void ajouterAnalyse(@RequestBody AjouterAnalyseRequest request);
}
