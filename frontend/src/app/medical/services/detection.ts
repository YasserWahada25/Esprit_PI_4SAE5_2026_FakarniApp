import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PatientUser {
  id: string;      // MongoDB ObjectId string
  nom: string;     // ← champ réel dans ton User model
  prenom: string;  // ← champ réel
  email: string;
  role: string;
  numTel?: string;
  adresse?: string;
}

export interface AnalyseIRMResponse {
  id: number;
  nomFichier: string;
  prediction: string;
  confidence: number;
  niveauRisque: string;
  couleurRisque: string;
  descriptionRisque: string;
  probMildDemented: number;   // ⚠️ valeur = 96.05, pas 0.9605
  probModerateDemented: number;
  probNonDemented: number;
  probVeryMildDemented: number;
  dateAnalyse: string;
  patientId: string;          // ✅ string, pas number
  conseilMedecin?: string;
  notesCliniques?: string;
  dateModification?: string;
}

export interface DossierMedicalResponse {
  id: number;
  patientId: string;          // ✅ string, pas number
  dateCreation: string;
  dateDerniereMaj: string;
  dernierePrediction: string;
  dernierNiveauRisque: string;
  derniereCouleurRisque: string;
  nombreAnalyses: number;
  analyses: AnalyseIRMResponse[];
}
export interface UpdateDescriptionRequest {
  analyseId: number;
  descriptionRisque?: string;
  conseilMedecin?: string;
  notesCliniques?: string;
}

@Injectable({ providedIn: 'root' })
export class DetectionService {

  // Use gateway for all API calls (proxied through Angular proxy.conf.json)
  private readonly API_URL    = '/api/detection';
  private readonly DOSSIER_URL = '/api/dossiers';
  private readonly USER_URL    = '/api/users';

  constructor(private http: HttpClient) {}

  // ── Patients ──────────────────────────────────────────
  getAllPatients(): Observable<PatientUser[]> {
    return this.http.get<PatientUser[]>(`${this.USER_URL}/patients`);
  }

  // ── MRI Analysis ─────────────────────────────────────
  analyserIRM(imageFile: File, patientId: string): Observable<AnalyseIRMResponse> {
    const formData = new FormData();
    formData.append('image', imageFile);
    return this.http.post<AnalyseIRMResponse>(
      `${this.API_URL}/analyser?patientId=${patientId}`,
      formData
    );
  }

  getDossierByPatientId(patientId: string): Observable<DossierMedicalResponse> {
    return this.http.get<DossierMedicalResponse>(
      `${this.DOSSIER_URL}/patient/${patientId}`
    );
  }
  updateAnalyseDescription(request: UpdateDescriptionRequest): Observable<DossierMedicalResponse> {
    return this.http.put<DossierMedicalResponse>(
      `${this.DOSSIER_URL}/analyse/update-description`,
      request
    );
  }

  deleteAnalyse(analyseId: number): Observable<DossierMedicalResponse> {
    return this.http.delete<DossierMedicalResponse>(
      `${this.DOSSIER_URL}/analyse/${analyseId}`
    );
  }
}