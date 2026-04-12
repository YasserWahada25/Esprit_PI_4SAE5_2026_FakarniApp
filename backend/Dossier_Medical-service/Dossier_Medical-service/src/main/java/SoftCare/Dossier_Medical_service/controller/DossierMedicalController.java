package SoftCare.Dossier_Medical_service.controller;

import SoftCare.Dossier_Medical_service.dto.AjouterAnalyseRequest;
import SoftCare.Dossier_Medical_service.dto.DossierMedicalResponse;
import SoftCare.Dossier_Medical_service.dto.UpdateDescriptionRequest;
import SoftCare.Dossier_Medical_service.service.DossierMedicalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dossiers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class DossierMedicalController {

    private final DossierMedicalService service;

    // ══════════════════════════════════════════════════
    //   CREATE - Ajouter une analyse (appelé par Detection)
    // ══════════════════════════════════════════════════
    @PostMapping("/ajouter-analyse")
    public ResponseEntity<DossierMedicalResponse> ajouterAnalyse(
            @RequestBody AjouterAnalyseRequest request) {
        log.info("📨 Réception nouvelle analyse pour patient {}", request.getPatientId());
        DossierMedicalResponse response = service.ajouterAnalyse(request);
        return ResponseEntity.ok(response);
    }

    // ══════════════════════════════════════════════════
    //   READ - Récupérer dossier par patient
    // ══════════════════════════════════════════════════
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<DossierMedicalResponse> getDossierByPatientId(
            @PathVariable String patientId) {
        log.info("📂 Chargement dossier patient {}", patientId);
        return ResponseEntity.ok(service.getDossierByPatientId(patientId));
    }

    // ══════════════════════════════════════════════════
    //   READ - Récupérer dossier par ID
    // ══════════════════════════════════════════════════
    @GetMapping("/{id}")
    public ResponseEntity<DossierMedicalResponse> getDossierById(
            @PathVariable Long id) {
        log.info("📂 Chargement dossier {}", id);
        return ResponseEntity.ok(service.getDossierById(id));
    }

    // ══════════════════════════════════════════════════
    //   READ - Récupérer UNE analyse spécifique
    // ══════════════════════════════════════════════════
    @GetMapping("/analyse/{analyseId}")
    public ResponseEntity<DossierMedicalResponse.AnalyseIRMDossierResponse> getAnalyseById(
            @PathVariable Long analyseId) {
        log.info("📄 Chargement analyse {}", analyseId);
        return ResponseEntity.ok(service.getAnalyseById(analyseId));
    }

    // ══════════════════════════════════════════════════
    //   ✅ UPDATE - Modifier description & conseil médecin
    // ══════════════════════════════════════════════════
    @PutMapping("/analyse/update-description")
    public ResponseEntity<DossierMedicalResponse> updateDescription(
            @RequestBody UpdateDescriptionRequest request) {
        log.info("✏️ Mise à jour description analyse {}", request.getAnalyseId());
        DossierMedicalResponse response = service.updateDescription(request);
        return ResponseEntity.ok(response);
    }

    // ══════════════════════════════════════════════════
    //   ✅ DELETE - Supprimer une analyse du dossier
    // ══════════════════════════════════════════════════
    @DeleteMapping("/analyse/{analyseId}")
    public ResponseEntity<DossierMedicalResponse> supprimerAnalyse(
            @PathVariable Long analyseId) {
        log.info("🗑️ Suppression analyse {}", analyseId);
        DossierMedicalResponse response = service.supprimerAnalyse(analyseId);
        return ResponseEntity.ok(response);
    }
}