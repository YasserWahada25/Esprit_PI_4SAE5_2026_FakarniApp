import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AnalyseIRMResponse, DetectionService, DossierMedicalResponse } from '../services/detection';

@Component({
  selector: 'app-follow-up',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './follow-up.component.html',
  styleUrl: './follow-up.component.css'
})
export class FollowUpComponent implements OnInit {

  activeTab: 'overview' | 'dossier' | 'stats' = 'overview';
  dossier: DossierMedicalResponse | null = null;
  isLoading = false;
  errorMessage = '';
  readonly patientId = 1;

  editingAnalyse: AnalyseIRMResponse | null = null;
  editForm = { descriptionRisque: '', conseilMedecin: '', notesCliniques: '' };

  constructor(private detectionService: DetectionService) {}

  ngOnInit(): void { this.loadDossier(); }

  loadDossier(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.detectionService.getDossierByPatientId(this.patientId).subscribe({
      next: (dossier) => {
        dossier.analyses.sort((a, b) => 
          new Date(b.dateAnalyse).getTime() - new Date(a.dateAnalyse).getTime()
        );
        this.dossier = dossier;
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.status === 404 
          ? 'No medical record found.' : 'Error loading dossier.';
      }
    });
  }
// ── Nouvelles propriétés viewer ───────────────────────
viewerAnalyse: AnalyseIRMResponse | null = null;
viewerZoom       = 1;
viewerRotation   = 0;
viewerFlipped    = false;
viewerContrast   = 100;
viewerBrightness = 100;
viewerSaturation = 100;

// ── URL image IRM ─────────────────────────────────────
// Adaptez selon votre backend :
// Option A — Spring Boot sert les fichiers :
getMriImageUrl(nomFichier: string): string {
  return `http://localhost:8080/api/images/${nomFichier}`;
}
// Option B — assets Angular locaux :
// return `/assets/mri/${nomFichier}`;

onImageError(event: Event): void {
  const img = event.target as HTMLImageElement;
  img.src = 'assets/mri-placeholder.png'; // créez ce fichier
  img.style.opacity = '0.35';
  img.style.filter = 'grayscale(1)';
}

// ── Viewer : open / close ─────────────────────────────
openViewer(analyse: AnalyseIRMResponse): void {
  this.viewerAnalyse   = analyse;
  this.viewerZoom      = 1;
  this.viewerRotation  = 0;
  this.viewerFlipped   = false;
  this.viewerContrast  = 100;
  this.viewerBrightness = 100;
  this.viewerSaturation = 100;
}
closeViewer(): void { this.viewerAnalyse = null; }

// ── Viewer : contrôles ────────────────────────────────
zoomIn():  void { this.viewerZoom = Math.min(5, +(this.viewerZoom + 0.25).toFixed(2)); }
zoomOut(): void { this.viewerZoom = Math.max(0.25, +(this.viewerZoom - 0.25).toFixed(2)); }
rotateImage():      void { this.viewerRotation = (this.viewerRotation + 90) % 360; }
flipImage():        void { this.viewerFlipped = !this.viewerFlipped; }
increaseContrast(): void { this.viewerContrast   = Math.min(200, this.viewerContrast + 20); }
increaseBrightness():void { this.viewerBrightness = Math.min(200, this.viewerBrightness + 20); }
resetViewer():      void {
  this.viewerZoom = 1; this.viewerRotation = 0; this.viewerFlipped = false;
  this.viewerContrast = 100; this.viewerBrightness = 100; this.viewerSaturation = 100;
}

// ── Viewer : transform & filter CSS ──────────────────
getViewerTransform(): string {
  const flip = this.viewerFlipped ? ' scaleX(-1)' : '';
  return `scale(${this.viewerZoom}) rotate(${this.viewerRotation}deg)${flip}`;
}
getViewerFilter(): string {
  return `contrast(${this.viewerContrast}%) brightness(${this.viewerBrightness}%) saturate(${this.viewerSaturation}%)`;
}

// ── Gradient par stage ────────────────────────────────
getStageGradient(p: string): string {
  return ({
    Non_Demented:       'linear-gradient(90deg,#0891b2,#06b6d4)',
    Very_Mild_Demented: 'linear-gradient(90deg,#2563eb,#3b82f6)',
    Mild_Demented:      'linear-gradient(90deg,#7c3aed,#a78bfa)',
    Moderate_Demented:  'linear-gradient(90deg,#dc2626,#ef4444)'
  } as any)[p] || 'linear-gradient(90deg,#64748b,#94a3b8)';
}

// ── Barres de probabilité ─────────────────────────────
getProbBars(analyse: AnalyseIRMResponse) {
  return [
    { label: 'Non Demented',       value: analyse.probNonDemented,      color: '#0891b2' },
    { label: 'Very Mild Demented', value: analyse.probVeryMildDemented, color: '#2563eb' },
    { label: 'Mild Demented',      value: analyse.probMildDemented,     color: '#7c3aed' },
    { label: 'Moderate Demented',  value: analyse.probModerateDemented, color: '#dc2626' }
  ];
}
  openEditModal(analyse: AnalyseIRMResponse): void {
    this.editingAnalyse = analyse;
    this.editForm = {
      descriptionRisque: analyse.descriptionRisque || '',
      conseilMedecin: '',
      notesCliniques: ''
    };
  }

  closeEditModal(): void { this.editingAnalyse = null; }

  saveEdit(): void {
    if (!this.editingAnalyse) return;
    const request = {
      analyseId: this.editingAnalyse.id,
      descriptionRisque: this.editForm.descriptionRisque,
      conseilMedecin: this.editForm.conseilMedecin,
      notesCliniques: this.editForm.notesCliniques
    };
    this.detectionService.updateAnalyseDescription(request).subscribe({
      next: (updatedDossier) => {
        this.dossier = updatedDossier;
        this.closeEditModal();
        alert('✅ Analysis updated in database!');
      },
      error: (err) => { alert('❌ Error: ' + err.message); }
    });
  }

  deleteAnalyse(id: number, nomFichier: string): void {
    if (!confirm(`Delete "${nomFichier}"?`)) return;
    this.detectionService.deleteAnalyse(id).subscribe({
      next: (updatedDossier) => {
        this.dossier = updatedDossier;
        alert('✅ Analysis deleted from database!');
      },
      error: (err) => { alert('❌ Error: ' + err.message); }
    });
  }

  // ══════════════════════════════════════
  //   🎨 PALETTE UNIFIÉE — 4 stages
  //   Stage I   (Normal)     → Teal    #0891b2
  //   Stage II  (Very Mild)  → Blue    #2563eb
  //   Stage III (Mild)       → Violet  #7c3aed
  //   Stage IV  (Moderate)   → Red     #dc2626
  // ══════════════════════════════════════

  /**
   * Mapping couleurRisque (GREEN/YELLOW/ORANGE/RED) → nouvelle palette
   */
  getRiskColor(c: string): string {
    return {
      GREEN:  '#0891b2',  // teal
      YELLOW: '#2563eb',  // blue
      ORANGE: '#7c3aed',  // violet
      RED:    '#dc2626'   // red
    }[c] || '#64748b';
  }

  /**
   * Mapping prediction string → nouvelle palette
   */
  getStageColor(p: string): string {
    return {
      Non_Demented:       '#0891b2',
      Very_Mild_Demented: '#2563eb',
      Mild_Demented:      '#7c3aed',
      Moderate_Demented:  '#dc2626'
    }[p] || '#64748b';
  }

  /**
   * CSS class par stage pour styling avancé
   */
  getStageCssClass(p: string): string {
    return {
      Non_Demented:       'stage-normal',
      Very_Mild_Demented: 'stage-vigilance',
      Mild_Demented:      'stage-concern',
      Moderate_Demented:  'stage-critical'
    }[p] || '';
  }

  /**
   * Icône par stage
   */
  getStageIcon(p: string): string {
    return {
      Non_Demented:       'fa-solid fa-shield-heart',
      Very_Mild_Demented: 'fa-solid fa-magnifying-glass-chart',
      Mild_Demented:      'fa-solid fa-triangle-exclamation',
      Moderate_Demented:  'fa-solid fa-circle-exclamation'
    }[p] || 'fa-solid fa-brain';
  }

  /**
   * Numéro de stage (1-4)
   */
  getStageNumber(p: string): number {
    return { Non_Demented: 1, Very_Mild_Demented: 2, Mild_Demented: 3, Moderate_Demented: 4 }[p] || 0;
  }

  // Stats helpers
  predictionToScore(p: string): number {
    return { Non_Demented: 28, Very_Mild_Demented: 24, Mild_Demented: 19, Moderate_Demented: 12 }[p] || 20;
  }

  get mmseHistory() {
    if (!this.dossier?.analyses?.length) return [];
    return [...this.dossier.analyses].reverse().slice(-6).map(a => ({
      date: this.shortDate(a.dateAnalyse),
      score: this.predictionToScore(a.prediction),
      color: this.getStageColor(a.prediction)
    }));
  }

  getBarHeight(s: number): string { return (s / 30 * 100) + '%'; }

  get cognitiveStatus(): string {
    if (!this.dossier || this.dossier.analyses.length < 2) return 'Insufficient data';
    const stages = ['Non_Demented', 'Very_Mild_Demented', 'Mild_Demented', 'Moderate_Demented'];
    const d = stages.indexOf(this.dossier.analyses[0].prediction) - stages.indexOf(this.dossier.analyses[1].prediction);
    return d > 0 ? 'Declining' : d < 0 ? 'Improving' : 'Stable';
  }

  get cognitiveStatusColor(): string {
    return {
      Declining:         '#dc2626',
      Improving:         '#0891b2',
      Stable:            '#2563eb',
      'Insufficient data': '#64748b'
    }[this.cognitiveStatus] || '#64748b';
  }

  get stageDistribution() {
    if (!this.dossier?.analyses?.length) return [];
    const t = this.dossier.analyses.length;
    return [
      { key: 'Non_Demented',       label: 'Non Demented', color: '#0891b2', icon: 'fa-solid fa-shield-heart' },
      { key: 'Very_Mild_Demented', label: 'Very Mild',    color: '#2563eb', icon: 'fa-solid fa-magnifying-glass-chart' },
      { key: 'Mild_Demented',      label: 'Mild',         color: '#7c3aed', icon: 'fa-solid fa-triangle-exclamation' },
      { key: 'Moderate_Demented',  label: 'Moderate',     color: '#dc2626', icon: 'fa-solid fa-circle-exclamation' }
    ].map(s => ({
      ...s,
      count: this.dossier!.analyses.filter(a => a.prediction === s.key).length,
      pct: 0
    })).map(s => ({ ...s, pct: Math.round((s.count / t) * 100) })).filter(s => s.count > 0);
  }

  getTrend(i: number): string {
    if (!this.dossier || i >= this.dossier.analyses.length - 1) return '';
    const stages = ['Non_Demented', 'Very_Mild_Demented', 'Mild_Demented', 'Moderate_Demented'];
    const d = stages.indexOf(this.dossier.analyses[i].prediction) - stages.indexOf(this.dossier.analyses[i + 1].prediction);
    return d > 0 ? '⚠️ progression' : d < 0 ? '✅ improvement' : '✅ stable';
  }

  formatPrediction(p: string): string { return p?.replace(/_/g, ' ') || ''; }

  shortDate(d: string): string {
    return new Date(d).toLocaleDateString('en-GB', { day: '2-digit', month: 'short' });
  }
  formatDate(d: string): string {
    return new Date(d).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
  }
  formatDateTime(d: string): string {
    return new Date(d).toLocaleString('en-GB', {
      day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit'
    });
  }
}